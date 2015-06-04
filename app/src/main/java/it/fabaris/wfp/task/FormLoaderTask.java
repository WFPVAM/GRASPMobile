/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 ******************************************************************************/
package it.fabaris.wfp.task;

import android.os.AsyncTask;
import android.util.Log;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.reference.RootTranslator;
import org.javarosa.core.services.PrototypeManager;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.xform.parse.XFormParseException;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xform.util.XFormUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import it.fabaris.wfp.activities.FormEntryActivity;
import it.fabaris.wfp.application.Collect;
import it.fabaris.wfp.listener.FormLoaderListener;
import it.fabaris.wfp.logic.FileReferenceFactory;
import it.fabaris.wfp.logic.FormController;
import it.fabaris.wfp.utility.FileUtils;

/**
 * Background task for loading a form. 
 * This class is called when the user wants
 * compile a new form, here
 * all the objects needed to work on the
 * form through all the compilation process
 * are initialized
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 * @author Mureed  (mureedf@unops.org) --add ability to view and send image
 *
 */
public class FormLoaderTask extends AsyncTask<String, String, FormLoaderTask.FECWrapper> {

    private final static String t = "FormLoaderTask";
    //Classes needed to serialize objects. Need to put anything from JR in here.

    public final static String[] SERIALIABLE_CLASSES = {
            "org.javarosa.core.model.FormDef",
            "org.javarosa.core.model.GroupDef",
            "org.javarosa.core.model.QuestionDef",
            "org.javarosa.core.model.data.DateData",
            "org.javarosa.core.model.data.DateTimeData",
            "org.javarosa.core.model.data.DecimalData",
            "org.javarosa.core.model.data.GeoPointData",
            "org.javarosa.core.model.data.helper.BasicDataPointer",
            "org.javarosa.core.model.data.IntegerData",
            "org.javarosa.core.model.data.MultiPointerAnswerData",
            "org.javarosa.core.model.data.PointerAnswerData",
            "org.javarosa.core.model.data.SelectMultiData",
            "org.javarosa.core.model.data.SelectOneData",
            "org.javarosa.core.model.data.StringData",
            "org.javarosa.core.model.data.TimeData",
            "org.javarosa.core.services.locale.TableLocaleSource",
            "org.javarosa.xpath.expr.XPathArithExpr",
            "org.javarosa.xpath.expr.XPathBoolExpr",
            "org.javarosa.xpath.expr.XPathCmpExpr",
            "org.javarosa.xpath.expr.XPathEqExpr",
            "org.javarosa.xpath.expr.XPathFilterExpr",
            "org.javarosa.xpath.expr.XPathFuncExpr",
            "org.javarosa.xpath.expr.XPathNumericLiteral",
            "org.javarosa.xpath.expr.XPathNumNegExpr",
            "org.javarosa.xpath.expr.XPathPathExpr",
            "org.javarosa.xpath.expr.XPathStringLiteral",
            "org.javarosa.xpath.expr.XPathUnionExpr",
            "org.javarosa.xpath.expr.XPathVariableReference"
    };

    private FormLoaderListener mStateListener;
    private String mErrorMsg;

    protected class FECWrapper {
        FormController controller;


        protected FECWrapper(FormController controller) {
            this.controller = controller;
        }


        protected FormController getController() {
            return controller;
        }


        protected void free() {
            controller = null;
        }
    }

    FECWrapper data;



    /**
     *  Initialize {@link FormEntryController} with {@link FormDef} from binary or from XML. If given
     *  an instance, it will be used to fill the {@link FormDef}.
     */
    @Override
    protected FECWrapper doInBackground(String... path) {
        FormEntryController fec = null;
        FormDef fd = null;
        FileInputStream fis = null;
        mErrorMsg = null;

        String formPath = path[0];

        File formXml = new File(formPath);
        String formHash = FileUtils.getMd5Hash(formXml);
        File formBin = new File(Collect.CACHE_PATH + File.separator + formHash + ".formdef");

        if (formBin.exists()) {
            /**
             *  if we have binary, deserialize binary
             */
            Log.i(t,"Attempting to load " + formXml.getName() + " from cached file: "+ formBin.getAbsolutePath());
            fd = deserializeFormDef(formBin);
            if (fd == null) {
                /**
                 *  some error occured with deserialization. Remove the file, and make a new .formdef
                 *  from xml
                 */
                Log.w(t,"Deserialization FAILED!  Deleting cache file: " + formBin.getAbsolutePath());
                formBin.delete();
            }
        }
        if (fd == null) {
            /**
             * no binary, read from xml
             */
            try {
                Log.i(t, "Attempting to load from: " + formXml.getAbsolutePath());
                fis = new FileInputStream(formXml);
                fd = XFormUtils.getFormFromInputStream(fis);
                if (fd == null) {
                    mErrorMsg = "Error reading XForm file";
                } else {
                    serializeFormDef(fd, formPath);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                mErrorMsg = e.getMessage();
            } catch (XFormParseException e) {
                mErrorMsg = e.getMessage();
                e.printStackTrace();
            } catch (Exception e) {
                mErrorMsg = e.getMessage();
                e.printStackTrace();
            }
        }

        if (mErrorMsg != null) {
            return null;
        }

        /**
         *  new evaluation context for function handlers
         */
        EvaluationContext ec = new EvaluationContext();
        fd.setEvaluationContext(ec);

        /**
         *  create FormEntryController from formdef
         */
        FormEntryModel fem = new FormEntryModel(fd);
        fec = new FormEntryController(fem);

        try {
            /**
             *  import existing data into formdef
             */
            if (FormEntryActivity.mInstancePath != null) {
                /**
                 *  This order is important. Import data, then initialize.
                 */
                try {
                    Thread.sleep(1000);
                    importData(FormEntryActivity.mInstancePath, fec);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Thread.sleep(1000);
                fd.initialize(false);
            } else {
                fd.initialize(true);
            }
        } catch (RuntimeException e) {
            mErrorMsg = e.getMessage();
            return null;
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        /**
         *  set paths to /sdcard/odk/forms/formfilename-media/
         */
        String formFileName = formXml.getName().substring(0, formXml.getName().lastIndexOf("."));



        /**
         *  Remove previous forms
         */
        ReferenceManager._().clearSession();

        /**
         *  This should get moved to the Application Class
         */
        if (ReferenceManager._().getFactories().length == 0) {
            /**
             *  this is /sdcard/odk
             */
            ReferenceManager._().addReferenceFactory(
                    new FileReferenceFactory(Collect.FABARISODK_ROOT));
        }

        /**
         *  Set jr://... to point to /sdcard/odk/forms/filename-media/
         */
        ReferenceManager._().addSessionRootTranslator(
                new RootTranslator("jr://images/", "jr://file/forms/" + formFileName + "-media/"));
        ReferenceManager._().addSessionRootTranslator(
                new RootTranslator("jr://audio/", "jr://file/forms/" + formFileName + "-media/"));
        ReferenceManager._().addSessionRootTranslator(
                new RootTranslator("jr://video/", "jr://file/forms/" + formFileName + "-media/"));

        /**
         *  clean up vars
         */
        fis = null;
        fd = null;
        formBin = null;
        formXml = null;
        formPath = null;


        FormController fc = new FormController(fec);
        data = new FECWrapper(fc);
        return data;

    }


    public boolean importData(String filePath, FormEntryController fec) {
        /**
         *  convert files into a byte array
         */
        byte[] fileBytes = FileUtils.getFileAsBytes(new File(filePath));

        /**
         *  get the root of the saved and template instances
         */
        TreeElement savedRoot = XFormParser.restoreDataModel(fileBytes, null).getRoot();
        TreeElement templateRoot = fec.getModel().getForm().getInstance().getRoot().deepCopy(true);

        //savedRoot contains the value inserted by the user during the compilation process. There are all the values, but saved as strings (datatype=0)
        //templateRoot contains only the skeleton of the form, without the value inserted by the user. It has just the value of the Labels and the client version

        /**
         *  weak check for matching forms
         */
        if (!savedRoot.getName().equals(templateRoot.getName()) || savedRoot.getMult() != 0) {
            Log.e(t, "Saved form instance does not match template form definition");
            return false;
        } else {
            /**
             *  populate the data model
             */
            TreeReference tr = TreeReference.rootRef();
            tr.add(templateRoot.getName(), TreeReference.INDEX_UNBOUND);
            templateRoot.populate(savedRoot, fec.getModel().getForm());
            //*************************************************										29/11/2013
            ArrayList<String> str = new ArrayList<String>();
            /**
             * take the answers
             */
            str = treePreorder(savedRoot, str);
            str.remove(0);

            /**
             * take the model
             */
            ArrayList<TreeElement> treeMod = new ArrayList<TreeElement>();
            treeMod = treeModel(templateRoot, treeMod); //put the templateRoot's element
            //in the treeMod
            setImageDataType(templateRoot); //Add the DataType (7 for radio, 3 for decimal)


            //remove it so that the image will not be displayed in the view
            /**
             * Match the answers to the model
             */
//            for(int j=0; j < str.size(); j++)
//            {
//                if (treeMod.get(j) != null && treeMod.get(j).dataType == Constants.DATATYPE_BINARY && treeMod.get(j).getValue() != null && treeMod.get(j).getValue().getDisplayText().indexOf("jpg") > 0) {
//                   String x= filePath.substring(0, filePath.lastIndexOf("/") + 1) + str.get(j);
//                   treeMod.get(j).getValue().setValue(x);
//                }
//                }

            //*************************************************

            /**
             *  populated model to current form
             */
            fec.getModel().getForm().getInstance().setRoot(templateRoot);

            /**
             *  fix any language issues
             *  : http://bitbucket.org/javarosa/main/issue/5/itext-n-appearing-in-restored-instances
             */
            if (fec.getModel().getLanguages() != null) {
                fec.getModel()
                        .getForm()
                        .localeChanged(fec.getModel().getLanguage(),
                                fec.getModel().getForm().getLocalizer());
            }

            return true;

        }
    }


    /**
     * Read serialized {@link FormDef} from file and recreate as object.
     * @param formDef
     * @param formDef serialized FormDef file
     * @return {@link FormDef} object
     * @return
     */
    public FormDef deserializeFormDef(File formDef) {

        // TODO: any way to remove reliance on jrsp?

        /**
         *  need a list of classes that formdef uses
         */
        PrototypeManager.registerPrototypes(SERIALIABLE_CLASSES);
        FileInputStream fis = null;
        FormDef fd = null;
        try {
            /**
             *  create new form def
             */
            fd = new FormDef();
            fis = new FileInputStream(formDef);
            DataInputStream dis = new DataInputStream(fis);

            /**
             *  read serialized formdef into new formdef
             */
            fd.readExternal(dis, ExtUtil.defaultPrototypes());
            dis.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fd = null;
        } catch (IOException e) {
            e.printStackTrace();
            fd = null;
        } catch (DeserializationException e) {
            e.printStackTrace();
            fd = null;
        } catch (Exception e) {
            e.printStackTrace();
            fd = null;
        }

        return fd;
    }


    /**
     * Write the FormDef to the file system as a binary blog.
     * @param filepath path to the form file
     * @param fd
     * @param filepath
     */
    public void serializeFormDef(FormDef fd, String filepath) {
        /**
         *  calculate unique md5 identifier
         */
        String hash = FileUtils.getMd5Hash(new File(filepath));
        File formDef = new File(Collect.CACHE_PATH + File.separator + hash + ".formdef");

        /**
         *  formdef does not exist, create one.
         */
        if (!formDef.exists()) {
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(formDef);
                DataOutputStream dos = new DataOutputStream(fos);
                fd.writeExternal(dos);
                dos.flush();
                dos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPostExecute(FECWrapper wrapper) {
        try{
            synchronized (this) {
                if (mStateListener != null) {
                    if (wrapper == null) {
                        mStateListener.loadingError(mErrorMsg);
                    } else {
                        //THREAD PER L'APERTURA DELLA FORM DAL DATABASE
                        mStateListener.loadingComplete(wrapper.getController());
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void setFormLoaderListener(FormLoaderListener sl) {
        synchronized (this) {
            mStateListener = sl;
        }
    }


    public void destroy() {
        if (data != null) {
            data.free();
            data = null;
        }

    }

    public void setImageDataType(TreeElement tree)
    {
        if(tree == null);
        if(tree.dataType == 12)
        {
            //tree.dataType = 10;
            //tree.
        }
        Log.e("IL NUOVO DATATYPE E'", String.valueOf(tree.dataType));

        int num = tree.getNumChildren();
        for (int n=0; n<num; n++)
        {
            TreeElement child = tree.getChildAt(n);
            setImageDataType(child);
        }
    }

    public ArrayList<String> treePreorder(TreeElement tree, ArrayList<String> list)
    {
        if(tree == null);
        if(tree.getValue() == null)
            list.add(null);
        else if(tree.getValue() != null)
            list.add((String) tree.getValue().getValue());

        int num = tree.getNumChildren();
        for (int n=0; n<num; n++)
        {
            TreeElement child = tree.getChildAt(n);
            treePreorder(child, list);
        }
        return list;
    }

    public ArrayList<TreeElement> treeModel(TreeElement tree, ArrayList<TreeElement> list)
    {
        if(tree == null);
        if(!tree.getName().equals("data"))
            list.add(tree);

        int num = tree.getNumChildren();
        for (int n=0; n<num; n++)
        {
            TreeElement child = tree.getChildAt(n);
            treeModel(child, list);
        }
        return list;
    }
}

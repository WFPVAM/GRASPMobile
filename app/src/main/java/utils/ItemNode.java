package utils;

import java.util.ArrayList;

public class ItemNode {
    public String type;
    public String title;
    public String link;
    public int index;
    public ArrayList<ItemNode> sons;
    public ItemNode parent;

    public int nsons;

    public ItemNode()
    {
        title = null;
        type = null;
        link = null;
        sons = new ArrayList<ItemNode>();
        parent = null;

        nsons = 0;
    }

    public ItemNode(String title)
    {
        this.type = type;
        this.title = title;
        this.link = link;

        nsons = 0;
    }
    public ItemNode(String type, String title, String link)
    {
        this.type = type;
        this.title = title;
        this.link = link;
    }

    public int getIndex()
    {
        return index;
    }

    public void setIndex(int i)
    {
        index = i;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public void setLink(String link)
    {
        this.link = link;
    }

    public String getLink()
    {
        return link;
    }

    public String getTitle()
    {
        return title;
    }

    public String getType()
    {
        return type;
    }

    public ArrayList<ItemNode> getSons()
    {
        return sons;
    }

    public void setSons(ArrayList<ItemNode> sons)
    {
        this.sons = sons;
    }

    public void setSon(ItemNode son)
    {
        sons.add(son);
    }

    public void setParent(ItemNode itemNode)
    {
        parent = itemNode;
    }

    public ItemNode getParent()
    {
        return parent;
    }

    public ArrayList inserisci(ArrayList list, ItemNode node)
    {
        list.add(node);
        return list;
    }
}

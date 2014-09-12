package utils;

import org.javarosa.core.model.instance.TreeElement;

public class Queue
{
    private int first, last, size;
    private TreeElement[] storage;
    private static final int DEFAULTSIZE = 100;

    public Queue()
    {
        this(DEFAULTSIZE);
    }

    public Queue(int n)
    {
        size = n;
        storage = new TreeElement[size];
        first = last = -1;
    }

    public boolean isFull()
    {
        return ((first == 0) && (last == size - 1)) || (first == last + 1);
    }

    public boolean isEmpty()
    {
        return first == -1;
    }

    public void enqueue(TreeElement el)
    {
        if(!isFull())
            if ((last == size - 1) || (last == -1))
            {
                storage[0] = el; last = 0;
                if (first == -1) //caso coda vuota
                    first=0;
            }
            else
                storage[++last] = el;
    }

    public TreeElement dequeue()
    {
        TreeElement tmp = null;
        if(!isEmpty())
        {
            tmp = storage[first];
            if (first == last) //caso unico elemento
                last = first = -1;
            else if (first == size - 1)
                first = 0;
            else first++;
        }
        return tmp;
    }

    public void printAll()
    {
        if(isEmpty())
            System.out.println("Coda vuota.");
        else
        {
            int i = first;
            do
            {
                System.out.print(storage[i] + " ");
                i = (i + 1) % size;
            }
            while(i != ((last + 1) % size));
            System.out.println();
        }
    }
}
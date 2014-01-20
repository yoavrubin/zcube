package net.ftod.zcube.zdd;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * <h1>Algebra of sets of trees</h1>
 * 
 * <p>
 * Representation of sets of trees that support binary serialization, and functions to compute set of trees and set of subtrees as {@link ZDD}.
 * </p>
 * 
 * TODO Document sum and product operations.
 * 
 * @author <a href="mailto:fabien.todescato@gmail.com">Fabien Todescato</a>
 */
public abstract class ZDDTree {

    /**
     * Empty set of trees.
     */
    public static final ZDDTree BOT = new ZDDTree() {

        @Override
        protected ZDD trees(final ZDDPredicateCache eq, final ZDDOperationCache cu, final ZDDOperationCache un, final long h)
        {
            return ZDD.BOT;
        }

        @Override
        protected ZDD subtrees(final ZDDPredicateCache eq, final ZDDOperationCache cu, final ZDDOperationCache un, final long h)
        {
            return ZDD.BOT;
        }

        @Override
        protected Type type()
        {
            return Type.BOT;
        }

        @Override
        protected void _write(final DataOutputStream dos) throws IOException
        {
        }
    };

    /**
     * Singleton containing the empty tree.
     */
    public static final ZDDTree TOP = new ZDDTree() {

        @Override
        protected ZDD trees(final ZDDPredicateCache eq, final ZDDOperationCache cu, final ZDDOperationCache un, final long h)
        {
            return ZDD.TOP;
        }

        @Override
        protected ZDD subtrees(final ZDDPredicateCache eq, final ZDDOperationCache cu, final ZDDOperationCache un, final long h)
        {
            return ZDD.TOP;
        }

        @Override
        protected Type type()
        {
            return Type.BOT;
        }

        @Override
        protected void _write(final DataOutputStream dos) throws IOException
        {
        }
    };

    /**
     * Prefix a {@link ZDDTree} with a symbol.
     */
    public static ZDDTree prefix(final String p, final ZDDTree t)
    {
        return new ZDDTreePrefix(p, t);
    }

    /**
     * Prefix a {@link ZDDTree} with a path represented as a sequence of symbols.
     */
    public static ZDDTree prefix(final ZDDTree t, final String... p)
    {
        return prefix(Arrays.asList(p), t);
    }

    /**
     * Prefix a {@link ZDDTree} with a path represented as a sequence of symbols.
     */
    public static ZDDTree prefix(final Iterable<String> p, final ZDDTree t)
    {
        return prefix(p.iterator(), t);
    }

    /**
     * Prefix a {@link ZDDTree} with a path represented as a sequence of symbols.
     */
    public static ZDDTree prefix(final Iterator<String> p, final ZDDTree t)
    {
        if (p.hasNext()) {
            return new ZDDTreePrefix(p.next(), prefix(p, t));
        }

        return t;
    }

    public static ZDDTree branch(final String... p)
    {
        return branch(Arrays.asList(p));
    }

    public static ZDDTree branch(final Iterable<String> p)
    {
        return prefix(p.iterator(), TOP);
    }

    /**
     * <h3>Exterior product of a sequence of {@link ZDDTree}</h3>
     */
    public static ZDDTree product(final ZDDTree... ts)
    {
        return new ZDDTreeProduct(ts);
    }

    public static ZDDTree product(final Collection<ZDDTree> ts)
    {
        return product(array(ts));
    }

    public static ZDDTree sum(final ZDDTree... ts)
    {
        return new ZDDTreeSum(ts);
    }

    public static ZDDTree sum(final Collection<ZDDTree> ts)
    {
        return sum(array(ts));
    }

    public static ZDD trees(final ZDDTree t)
    {
        return trees(t, new ZDDPredicateCache(), new ZDDOperationCache(), new ZDDOperationCache());
    }

    static ZDD trees(final ZDDTree t, final ZDDPredicateCache eq, final ZDDOperationCache cu, final ZDDOperationCache un)
    {
        return t.trees(eq, cu, un, 1L);
    }

    public static ZDD subtrees(final ZDDTree t)
    {
        return subtrees(t, new ZDDPredicateCache(), new ZDDOperationCache(), new ZDDOperationCache());
    }

    static ZDD subtrees(final ZDDTree t, final ZDDPredicateCache eq, final ZDDOperationCache cu, final ZDDOperationCache un)
    {
        return t.subtrees(eq, cu, un, 1L);
    }

    protected abstract ZDD trees(ZDDPredicateCache eq, ZDDOperationCache cu, ZDDOperationCache un, long h);

    protected abstract ZDD subtrees(ZDDPredicateCache eq, ZDDOperationCache cu, ZDDOperationCache un, long h);

    protected static final ZDD[] mapTrees(final ZDDPredicateCache eq, final ZDDOperationCache cu, final ZDDOperationCache un, final long h, final ZDDTree[] ts)
    {
        final int n = ts.length;
        final ZDD[] zdds = new ZDD[n];

        for (int i = 0; i < n; ++i) {
            zdds[i] = ts[i].trees(eq, cu, un, h);
        }

        return zdds;
    }

    protected static final ZDD[] mapSubtrees(final ZDDPredicateCache eq, final ZDDOperationCache cu, final ZDDOperationCache un, final long h, final ZDDTree[] ts)
    {
        final int n = ts.length;
        final ZDD[] zdds = new ZDD[n];

        for (int i = 0; i < n; ++i) {
            zdds[i] = ts[i].subtrees(eq, cu, un, h);
        }

        return zdds;
    }

    protected static final long djb2(final long seed, final String string)
    {
        long hash = 5381L;

        hash = 33L * hash ^ seed >>> 24 & 0xFF;
        hash = 33L * hash ^ seed >>> 16 & 0xFF;
        hash = 33L * hash ^ seed >>> 8 & 0xFF;
        hash = 33L * hash ^ seed & 0xFF;

        for (int i = 0; i < string.length(); ++i) {
            hash = 33L * hash ^ string.charAt(i);
        }

        return hash;
    }

    private static ZDDTree[] array(final Collection<ZDDTree> c)
    {
        final ZDDTree[] a = new ZDDTree[c.size()];
        c.toArray(a);
        return a;
    }

    protected enum Type {
        BOT {
            @Override
            protected ZDDTree read(final DataInputStream dis)
            {
                return ZDDTree.BOT;
            }
        },
        TOP {
            @Override
            protected ZDDTree read(final DataInputStream dis)
            {
                return ZDDTree.TOP;
            }
        },
        Prefix {
            @Override
            protected ZDDTree read(final DataInputStream dis) throws IOException
            {
                return ZDDTreePrefix._read(dis);
            }
        },
        Product {
            @Override
            protected ZDDTree read(final DataInputStream dis) throws IOException
            {
                return ZDDTreeProduct._read(dis);
            }
        },
        Sum {
            @Override
            protected ZDDTree read(final DataInputStream dis) throws IOException
            {
                return ZDDTreeSum._read(dis);
            }
        };

        protected abstract ZDDTree read(DataInputStream dis) throws IOException;
    }

    protected abstract Type type();

    public static ZDDTree read(final DataInputStream dis) throws IOException
    {
        return Type.values()[dis.readByte()].read(dis);
    }

    public void write(final DataOutputStream dos) throws IOException
    {
        dos.writeByte(type().ordinal());
        _write(dos);
    }

    protected abstract void _write(DataOutputStream dos) throws IOException;

    public static ZDDTree[] readArray(final DataInputStream dis) throws IOException
    {
        final int length = dis.readInt();
        final ZDDTree[] array = new ZDDTree[length];

        for (int i = 0; i < length; ++i) {
            array[i] = read(dis);
        }

        return array;
    }

    public static void writeArray(final ZDDTree[] ts, final DataOutputStream dos) throws IOException
    {
        final int length = ts.length;

        dos.writeInt(length);

        for (int i = 0; i < length; ++i) {
            ts[i].write(dos);
        }
    }

}

final class ZDDTreePrefix extends ZDDTree {

    private final String prefix;
    private final ZDDTree treeSet;

    ZDDTreePrefix(final String prefix, final ZDDTree treeSet) {
        super();
        this.treeSet = treeSet;
        this.prefix = prefix;
    }

    static ZDDTree _read(final DataInputStream dis) throws IOException
    {
        final String prefix = dis.readUTF();
        final ZDDTree treeSet = read(dis);

        return new ZDDTreePrefix(prefix, treeSet);
    }

    @Override
    protected void _write(final DataOutputStream dos) throws IOException
    {
        dos.writeUTF(prefix);
        treeSet.write(dos);
    }

    @Override
    protected ZDD trees(final ZDDPredicateCache eq, final ZDDOperationCache cu, final ZDDOperationCache un, final long h)
    {
        final long h1 = djb2(h, prefix);

        return ZDD.crossUnion(eq, cu, un, ZDD.singleton(h1), treeSet.trees(eq, cu, un, h1));
    }

    @Override
    protected ZDD subtrees(final ZDDPredicateCache eq, final ZDDOperationCache cu, final ZDDOperationCache un, final long h)
    {
        final long h1 = djb2(h, prefix);

        return ZDD.union(eq, un, ZDD.TOP, ZDD.crossUnion(eq, cu, un, ZDD.singleton(h1), treeSet.subtrees(eq, cu, un, h1)));
    }

    @Override
    protected Type type()
    {
        return Type.Prefix;
    }

}

final class ZDDTreeProduct extends ZDDTree {

    private final ZDDTree[] ts;

    ZDDTreeProduct(final ZDDTree[] ts) {
        super();
        this.ts = ts;
    }

    static ZDDTreeProduct _read(final DataInputStream dis) throws IOException
    {
        return new ZDDTreeProduct(readArray(dis));
    }

    @Override
    protected void _write(final DataOutputStream dos) throws IOException
    {
        writeArray(ts, dos);
    }

    @Override
    protected ZDD trees(final ZDDPredicateCache eq, final ZDDOperationCache cu, final ZDDOperationCache un, final long h)
    {
        return ZDD.crossUnion(eq, cu, un, mapTrees(eq, cu, un, h, ts));
    }

    @Override
    protected ZDD subtrees(final ZDDPredicateCache eq, final ZDDOperationCache cu, final ZDDOperationCache un, final long h)
    {
        return ZDD.crossUnion(eq, cu, un, mapSubtrees(eq, cu, un, h, ts));
    }

    @Override
    protected Type type()
    {
        return Type.Product;
    }

}

final class ZDDTreeSum extends ZDDTree {

    private final ZDDTree[] ts;

    ZDDTreeSum(final ZDDTree[] ts) {
        super();
        this.ts = ts;
    }

    static ZDDTreeSum _read(final DataInputStream dis) throws IOException
    {
        return new ZDDTreeSum(readArray(dis));
    }

    @Override
    protected void _write(final DataOutputStream dos) throws IOException
    {
        writeArray(ts, dos);
    }

    @Override
    protected ZDD trees(final ZDDPredicateCache eq, final ZDDOperationCache cu, final ZDDOperationCache un, final long h)
    {
        return ZDD.union(eq, un, mapTrees(eq, cu, un, h, ts));
    }

    @Override
    protected ZDD subtrees(final ZDDPredicateCache eq, final ZDDOperationCache cu, final ZDDOperationCache un, final long h)
    {
        return ZDD.union(eq, un, mapSubtrees(eq, cu, un, h, ts));
    }

    @Override
    protected Type type()
    {
        return Type.Sum;
    }

}
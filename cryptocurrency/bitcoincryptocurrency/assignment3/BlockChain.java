// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockChain {

    private class BlockNode {
        Block block;
        BlockNode parent;
        List<BlockNode> children;
        UTXOPool utxoPool;
        int height;

        public BlockNode(Block block, BlockNode parent, UTXOPool utxoPool) {
            this.block = block;
            this.parent = parent;
            this.utxoPool = utxoPool;
            this.height = parent != null ? parent.height + 1 : 1;
            this.children = new ArrayList<>();
            if (parent != null) {
                parent.children.add(this);
            }
        }
    }

    public static final int CUT_OFF_AGE = 10;
    private HashMap<ByteArrayWrapper, BlockNode> theChain;
    private TransactionPool txPool;
    private BlockNode nodeMaxHeight;

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        theChain = new HashMap<>();
        txPool = new TransactionPool();
        UTXOPool utxoPool = new UTXOPool();
        byte[] prevBlockHash = genesisBlock.getPrevBlockHash();
        if (prevBlockHash != null) return;
        coinBase2utxoPool(genesisBlock, utxoPool);
        BlockNode genesisBlockNode = new BlockNode(genesisBlock,
                                                   null,
                                                   utxoPool);
        ByteArrayWrapper blockId = new ByteArrayWrapper(genesisBlock.getHash());
        theChain.put(blockId, genesisBlockNode);
        nodeMaxHeight = genesisBlockNode;
    }

    private void coinBase2utxoPool(Block block, UTXOPool utxoPool) {
        int i = 0;
        Transaction cb = block.getCoinbase();
        txPool.addTransaction(cb);
        for (Transaction.Output op: cb.getOutputs()) {
            UTXO utxo = new UTXO(cb.getHash(), i++);
            utxoPool.addUTXO(utxo, op);
        }
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        return nodeMaxHeight.block;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        return new UTXOPool(nodeMaxHeight.utxoPool);
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        return txPool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        System.out.print("1 " + block + " ");
        if (block == null || block.getPrevBlockHash() == null) return false;
        System.out.print("2 ");
        ByteArrayWrapper parentBlockId = new ByteArrayWrapper(block.getPrevBlockHash());
        BlockNode parentNode = theChain.get(parentBlockId);
        if (parentNode == null) return false;
        System.out.print("3 " + parentNode + " ");
        if (parentNode.height < nodeMaxHeight.height - CUT_OFF_AGE) return false;
        System.out.print("4 ");
        TxHandler handler = new TxHandler(new UTXOPool(parentNode.utxoPool));
        for (Transaction tX: block.getTransactions()) {
            System.out.print("5 " + tX);
            if (handler.isValidTx(tX) == false) return false;
        }
        System.out.print("6 ");
        UTXOPool utxoPool = handler.getUTXOPool();
        coinBase2utxoPool(block, utxoPool);
        BlockNode node = new BlockNode(block, parentNode, utxoPool);
        theChain.put(new ByteArrayWrapper(block.getHash()), node);
        if (node.height > nodeMaxHeight.height) nodeMaxHeight = node;
        System.out.println("7 ");
        return true;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        txPool.addTransaction(tx);
    }
}

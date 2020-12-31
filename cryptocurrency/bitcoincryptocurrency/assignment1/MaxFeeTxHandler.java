import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

public class TxHandler {

    private UTXOPool pool;
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        this.pool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
	ArrayList<Transaction.Input> ips = tx.getInputs();
	Set<UTXO> sutx = new HashSet<UTXO>();
	double inputSum = 0, outputSum = 0;
	for (int i = 0; i < ips.size(); i++) {
	    Transaction.Input ip = ips.get(i);
	    UTXO u = new UTXO(ip.prevTxHash, ip.outputIndex);
	    if (this.pool.contains(u) == false) {
		// (1)
		return false;
	    }
	    Transaction.Output op = this.pool.getTxOutput(u);
	    if (Crypto.verifySignature(op.address, tx.getRawDataToSign(i),
				       ip.signature) == false) {
		// (2)
		return false;
	    }
	    if (sutx.contains(u)) {
		// (3)
		return false;
	    }
	    sutx.add(u);
	    inputSum += op.value;
	}
	ArrayList<Transaction.Output> ops = tx.getOutputs();
	for (int i = 0; i < ops.size(); i++) {
	    Transaction.Output op = ops.get(i);
	    if (op.value < 0) {
		// (4)
		return false;
	    }
	    outputSum += op.value;
	}
	if (outputSum > inputSum) {
	    // (5)
	    return false;
	}
	return true;
    }

    public  Transaction[]  handleTxs (Transaction[] possibleTxs)
    {

    }

}

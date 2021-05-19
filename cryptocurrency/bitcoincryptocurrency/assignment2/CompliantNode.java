import java.util.ArrayList;
import java.util.Set;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {

    private int followees[];
    private Set<Transaction> initialTxns;
    private double p_graph, p_malicious, p_txDistribution;
    private int numRounds, currentRound;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        this.p_graph = p_graph;
	this.p_malicious = p_malicious;
	this.p_txDistribution = p_txDistribution;
	this.numRounds = numRounds;
	this.currentRound = 0;
    }

    public void setFollowees(boolean[] followees) {
        // IMPLEMENT THIS
	this.followees = new int[followees.length];
	int i = 0;
	for (boolean f: followees) {
	    this.followees[i] = f ? 1 : 0;
	    i++;
	}
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        // IMPLEMENT THIS
	System.out.println(this.hashCode() + " setPendingTransaction " + pendingTransactions);
	this.initialTxns = pendingTransactions;
    }

    public Set<Transaction> sendToFollowers() {
        // IMPLEMENT THIS
	System.out.println(this.hashCode() + " sendToFollowers ");

	return this.initialTxns;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
	this.currentRound++;
	if (this.currentRound < this.numRounds - 1) {
	    for (Candidate c: candidates) {
		this.initialTxns.add(c.tx);
	    }
	}
    }
}

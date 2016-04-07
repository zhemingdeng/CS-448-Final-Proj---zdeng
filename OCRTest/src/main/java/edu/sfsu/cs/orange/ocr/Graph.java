package edu.sfsu.cs.orange.ocr;

/**
 * Created by MICHAEL DENG on 4/1/2016.
 * Using a 2-D integer array to represent the relationship between each two nodes.
 * int[x][y]==-1, there is no connection between x and y
 * int[x][y]==0, x and y indicate the same vertex
 * int[x][y]=="and value greater than 0", there is a connection between x and y, and the weight is the value
 */
public class Graph {
    private final int[][] mInter_Nodes;

    public Graph(int[][] inter_nodes){
        this.mInter_Nodes=inter_nodes;
    }

    protected void addEdge(int NodeA, int NodeB){
        mInter_Nodes[NodeA][NodeB]=1; //two node connected
        System.out.println("Edge between "+NodeA+" and "+NodeB+" connected"); //for test only
    }

    protected void removeEdge(int node_a, int node_b){
        mInter_Nodes[node_a][node_b]=0;
        System.out.println("Edge between "+node_a+" and "+node_b+" disconnected"); // for test only
    }

    protected int num_nodes(){
        return mInter_Nodes.length;
    }

    //check if the second_node is the neighbor of the source node, return boolean value
    protected boolean adjNodes(int nodesource, int second_node){
        boolean connected=false;
        if(mInter_Nodes[nodesource][second_node]!=-1){
            connected=true; //if there exists connection between two nodes, return the second node
        }							//else return source node
        System.out.println(connected);
        return connected;
    }

    //return the total number of vertex
    protected int getSize(){
        return mInter_Nodes.length;
    }

    //return the weight between two vertex
    protected int getWeight(int nodesource, int nodedest){
        return mInter_Nodes[nodesource][nodedest];
    }
}

package client;

/**
 * Created by TingMiao on 18/12/2016.
 */
public class Main {

    public static void main(String[] args){
        ClientTests	ct	=	new	ClientTests(new HttpClient());
        ct.runTests("http://cs.nott.ac.uk/~syn/webfiles/index.html");
    }
}

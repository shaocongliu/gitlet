import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;
import java.io.FileOutputStream;


public class Gitlet {
    GitletTree gitletTree;

    public Gitlet() {
        gitletTree = new GitletTree();
    }

    public static void main(String[] args) {
        Gitlet git = new Gitlet();
        if ((args.length == 0) || (args == null)) {
            System.out.println("Please enter an input");
        } else {
            String[] commands = args; String command = commands[0];
            String[] tokens = new String[commands.length - 1];
            System.arraycopy(commands, 1, tokens, 0, commands.length - 1);
            git.tryloadingGitlet();
            Scanner check = new Scanner(System.in);
            switch (command) {
                case "init": 
                    git.treeinit();
                    break;
                case "add":
                    git.treeadd(tokens[0]);
                    break; 
                case "commit": 
                    try {
                        git.treecommit(tokens[0]);
                    } catch (IndexOutOfBoundsException e) {
                        System.out.println("Please enter a commit message.");
                    }
                    break;
                case "rm": 
                    git.treeremove(tokens[0]);
                    break;
                case "log":
                    git.treelog();
                    break;
                case "global-log":
                    git.treegloballog();
                    break;
                case "find":
                    git.treefind(tokens[0]);
                    break;
                case "checkout":
                    git.treeDangerous(check);
                    if ((check.next()).equals("yes")) {
                        if (tokens.length == 2) {
                            git.treecheckout2(Integer.parseInt(tokens[0]), tokens[1]);
                        } else if (tokens.length == 1) {
                            git.treecheckout1(tokens[0]);
                        }
                    }
                    break;
                case "status":
                    git.treestatus();
                    break;
                case "branch":
                    git.treebranch(tokens[0]);
                    break;
                case "rm-branch":
                    git.treermbranch(tokens[0]);
                    break;
                case "reset":
                    git.treeDangerous(check);
                    if ((check.next()).equals("yes")) {
                        git.treereset(Integer.parseInt(tokens[0]));
                    }
                    break;
                case "merge":
                    git.treemerge(tokens[0]);
                    break;
                case "rebase":
                    git.treeDangerous(check);
                    if ((check.next()).equals("yes")) {
                        git.treerebase(tokens[0]);
                    }
                    break;
                case "i-rebase":
                    git.treeiRebase(tokens[0]);
                    break;
                default:
                    System.out.println("Invalid command.");  
                    break;
            }
            git.saveGitlet();
        }
    }

    private void treeinit() {
        gitletTree.initialize();
    }

    private void treecommit(String file) {
        gitletTree.commit(file);
    }

    private void treeadd(String file) {
        gitletTree.add(file);
    }

    private void treeremove(String file) {
        gitletTree.remove(file);
    }

    private void treelog() {
        gitletTree.log();
    }

    private void treegloballog() {
        gitletTree.globalLog();
    }

    private void treestatus() {
        gitletTree.status();
    }

    private void treefind(String message) {
        gitletTree.find(message);
    }

    private void treebranch(String branch) {
        gitletTree.branch(branch);
    }

    private void treecheckout2(int id, String input) {
        gitletTree.checkout(id, input);
    }

    private void treecheckout1(String input) {
        gitletTree.checkout(input);
    }

    private void treermbranch(String branch) {
        gitletTree.removeBranch(branch);
    }

    private void treerebase(String input) {
        gitletTree.rebase(input);
    }

    private void treereset(int id) {
        gitletTree.reset(id);
    }

    private void treemerge(String input) {
        gitletTree.merge(input);
    }

    private void treeiRebase(String input) {
        gitletTree.iRebase(input);
    }

    private void treeDangerous(Scanner check) {
        System.out.println("Warning: The command you entered "
            + "may alter the files in your working directory. "
            + "Uncommitted changes may be lost. Are you sure you want "
            + "to continue? (yes/no)");
    }

    private void tryloadingGitlet() {
        File myTreeFile = new File(".gitlet/myTree.ser");
        if (myTreeFile.exists()) {
            try {
                FileInputStream fileIn = new FileInputStream(myTreeFile);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);
                gitletTree = (GitletTree) objectIn.readObject();
            } catch (IOException e) {
                String msg = "IOException while loading myTree.";
                System.out.println(msg);
            } catch (ClassNotFoundException e) {
                String msg = "ClassNotFoundException while loading myTree.";
                System.out.println(msg);
            }
        }
    }

    private void saveGitlet() {
        try {
            File myTreeFile = new File(".gitlet/myTree.ser");
            FileOutputStream fileOut = new FileOutputStream(myTreeFile);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(gitletTree);
        } catch (IOException e) {
            String msg = "IOException while saving myGitlet.";
            System.out.println(msg);
        }
    }
}

import java.io.Serializable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.DateFormat;
import java.util.Iterator;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.List;

public class GitletTree implements Serializable {

    private HashMap<String, ArrayList<Integer>> branches;
    private HashMap<Integer, Node> commits;
    private HashSet<String> addedFiles;
    private HashSet<String> removedFiles;
    private HashSet<String> lastCommitFiles;
    private int id;
    private int pointer;
    private String currentBranch;
    private HashSet<String> addedFilesShortPath;

    public GitletTree() {
        branches = new HashMap<String, ArrayList<Integer>>();
        branches.put("master", new ArrayList<Integer>());
        currentBranch = "master";
        commits = new HashMap<Integer, Node>();
        addedFiles = new HashSet<String>();
        lastCommitFiles = new HashSet<String>();
        removedFiles = new HashSet<String>();
        id = 0;
        pointer = 0;
    }

    public void initialize() {
        File folder = new File(".gitlet");
        if (!folder.exists()) {
            folder.mkdir();
            commit("initial commit");
        } else {
            System.out.println(
                "A gitlet version control system already exists in the current directory.");
        }
    }

    private class Node implements Serializable {
        private int id;
        private Long longTime;
        private String formatedTime;
        private String message;
        private HashSet<String> commitedFiles;
        private HashMap<String, String> pathToPath;
        private String branch;

        private Node(int id, Long longTime, String formatedTime, String message, 
                HashSet<String> commitedFiles, HashMap<String, String> pathToPath, String branch) {
            this.id = id;
            this.longTime = longTime;
            this.formatedTime = formatedTime;
            this.message = message;
            this.commitedFiles = commitedFiles;
            this.pathToPath = pathToPath;
            this.branch = branch;
        }
    }

    //Below helper method is adapted from stackoverflow.
    private int compareFile(String fILEONE2, String fILETWO2) throws IOException  {  
        File f1 = new File(fILEONE2);
        File f2 = new File(fILETWO2);
        FileReader fR1 = new FileReader(f1);
        FileReader fR2 = new FileReader(f2);
        BufferedReader reader1 = new BufferedReader(fR1);
        BufferedReader reader2 = new BufferedReader(fR2);
        String line1 = null;
        String line2 = null;
        int flag = 1;
        while (((line1 = reader1.readLine()) != null) && ((line2 = reader2.readLine()) != null)) {
            if (!line1.equalsIgnoreCase(line2)) { 
                flag = 0; 
                break;
            }
        }
        reader1.close();
        reader2.close();
        return flag; //return 1 if equal, 0 if not equal;
    }

    public void add(String filename) {
        String directory = System.getProperty("user.dir");
        String orinFilename = filename;
        filename = directory + "/" + filename;
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        if (removedFiles.contains(orinFilename)) {
            removedFiles.remove(orinFilename);
            return;
        }
        if ((lastCommitFiles != null) && (lastCommitFiles.contains(filename))) {
            int compare = 0;
            try { 
                compare = compareFile(filename, commits.get(pointer).pathToPath.get(filename));
            } catch (IOException e) {
                System.out.println("There are problems");
            }
            if (1 == compare) {
                System.out.println("File has not been modified since the last commit.");
                return;
            }
        }
        addedFilesShortPath.add(orinFilename);
        addedFiles.add(filename);
    }

    public void commit(String message) {
        if ((addedFiles.size() == 0) && (!message.equals("initial commit"))) {
            System.out.println("No changes added to the commit.");
            return;
        }
        if (message == null) {
            System.out.println("Please enter a commit message.");
            return;
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        Long longTime = date.getTime();
        String formatedTime = dateFormat.format(date);
        HashSet<String> allFiles = (HashSet<String>) addedFiles.clone();
        String directory = System.getProperty("user.dir");
        for (String str : removedFiles) {
            lastCommitFiles.remove(directory + "/" + str);
        }
        Iterator<String> oldFileIter = lastCommitFiles.iterator();
        while (oldFileIter.hasNext()) {
            String nextOne = oldFileIter.next();
            if (!allFiles.contains(nextOne)) {
                allFiles.add(nextOne);
            }
        }

        HashMap<String, String> pathToPath = new HashMap<String, String>();
        HashSet<String> allNewFiles = new HashSet<String>();
        File folder = new File(".gitlet/commit" + id);
        if (!folder.exists()) {
            folder.mkdir();
            File newFile;
            File oldFile;
            for (String file : allFiles) {
                oldFile = new File(file);
                newFile = new File(".gitlet/commit" + id + "/" + oldFile.getName());
                if (!newFile.exists()) {
                    try {
                        Files.copy(oldFile.toPath(), newFile.toPath());
                        pathToPath.put(file, newFile.getPath());
                    } catch (IOException e) {
                        System.out.println("There are problems");
                    }
                }
                allNewFiles.add(oldFile.getPath());
            }
        }
        Node curCommit = new Node(id, longTime, 
                formatedTime, message, allNewFiles, pathToPath, currentBranch);

        branches.get(currentBranch).add(id);
        commits.put(id, curCommit);
        pointer = id;
        id += 1;
        addedFiles = new HashSet<String>();
        removedFiles = new HashSet<String>();
        lastCommitFiles = (HashSet<String>) allFiles.clone();
        addedFilesShortPath = new HashSet<String>();
    }

    public void remove(String filename) {
        String directory = System.getProperty("user.dir");
        String orinFilename = filename;
        filename = directory + "/" + filename;
        if (addedFiles.contains(filename)) {
            addedFiles.remove(filename);
            addedFilesShortPath.remove(orinFilename);
        } else if (lastCommitFiles.contains(filename)) {
            removedFiles.add(orinFilename);
        } else {
            System.out.println("No reason to remove the file.");
        }
    }

    public void log() {
        ArrayList list = branches.get(currentBranch);
        int index = list.size() - 1;
        while (index >= 0) {
            Node node = commits.get(list.get(index));
            System.out.println("====");
            System.out.println("Commit " + node.id + ".");
            System.out.println(node.formatedTime);
            System.out.println(node.message);
            index -= 1;
        }
    }

    public void globalLog() {
        for (Integer i : commits.keySet()) {
            Node node = commits.get(i);
            System.out.println("====");
            System.out.println("Commit " + node.id + ".");
            System.out.println(node.formatedTime);
            System.out.println(node.message);
        }
    }
    
    public void find(String message) {
        HashSet<Integer> ids = new HashSet<Integer>();
        for (Integer i : commits.keySet()) {
            Node node = commits.get(i);
            if (node.message.equals(message)) {
                ids.add(i);
                System.out.println(i);
            }
        }
        if (ids.size() == 0) {
            System.out.println("Found no commit with that message.");
        }
    }
    
    public void status() {
        System.out.println("=== Branches ===");
        for (String branch : branches.keySet()) {
            if (branch.equals(currentBranch)) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }

        System.out.println("=== Staged Files ===");
        for (String files : addedFilesShortPath) {
            System.out.println(files);
        }

        System.out.println("=== Files Marked for Removal ===");
        for (String files : removedFiles) {
            System.out.println(files);
        }
    }

    public void checkout(String string) {
        Node node = commits.get(pointer);
        String directory = System.getProperty("user.dir");
        String filename = directory + "/" + string;
        if ((!branches.containsKey(string)) && (!node.pathToPath.containsKey(filename))) {
            System.out.println(
                "File does not exist in the most recent commit, or no such branch exists.");
            return;
        }
        if (branches.containsKey(string)) {
            String branchName = string;
            if (currentBranch.equals(branchName)) {
                System.out.println("No need to checkout the current branch.");
                return;
            }
            ArrayList<Integer> branch = branches.get(branchName);
            pointer = branch.get(branch.size() - 1);
            currentBranch = branchName;  
        } else {
            if (!node.pathToPath.containsKey(filename)) {
                System.out.println(
                    "File does not exist in the most recent commit, or no such branch exists.");
                return;
            }
            File toBeReplaced = new File(filename);
            File toReplace = new File(node.pathToPath.get(filename));
            try {
                Files.copy(toReplace.toPath(), 
                    toBeReplaced.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.out.println("There are problems");
            }
        }
    }

    public void checkout(int thisid, String filename) {
        String directory = System.getProperty("user.dir");
        filename = directory + "/" + filename;
        if (!branches.get(currentBranch).contains(thisid)) {
            System.out.println("No commit with that id exists.");
            return;
        }
        if (!branches.containsKey(filename)) {
            Node node = commits.get(thisid);
            if (!node.commitedFiles.contains(filename)) {
                System.out.println("File does not exist in that commit.");
                return;
            }
            File toBeReplaced = new File(filename);
            File toReplace = new File(node.pathToPath.get(filename));
            if (toReplace.exists()) {
                try {
                    Files.copy(toReplace.toPath(), toBeReplaced.toPath());
                } catch (IOException e) {
                    System.out.println("There are problems");
                }
            } else {
                System.out.println("File does not exist in that commit.");
            }
        }
    }

    public void branch(String branchName) {
        if (branches.containsKey(branchName)) {
            System.out.println("A branch with that name already exists.");  
            return;
        }
        ArrayList<Integer> newList = (ArrayList<Integer>) branches.get(currentBranch).clone();
        branches.put(branchName, newList);
    }

    public void reset(int thisid) {
        if (!commits.containsKey(thisid)) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Node resetCommit = commits.get(thisid);
        ArrayList<Integer> branchArray = branches.get(resetCommit.branch);
        int newId = branchArray.indexOf(thisid);
        HashSet<String> commitFiles = resetCommit.commitedFiles;
        HashSet<String> curFiles = commits.get(pointer).commitedFiles;
        for (String file : curFiles) {
            if (commitFiles.contains(file)) {
                try {
                    File toCoverFile = new File(resetCommit.pathToPath.get(file));
                    File coveredFile = new File(file);
                    Files.copy(
                        toCoverFile.toPath(), 
                        coveredFile.toPath(), 
                        StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    System.out.println("There are problems");
                }  
            }
        }
        List<Integer> newList = branchArray.subList(0, newId + 1);
        ArrayList<Integer> newArrayList = new ArrayList();
        Iterator<Integer> iter = newList.iterator();
        while (iter.hasNext()) {
            newArrayList.add(iter.next());
        }
        pointer = thisid;
        branches.put(currentBranch, newArrayList);
    }

    public void removeBranch(String branchName) {
        if (!branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (branchName.equals(currentBranch)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        branches.remove(branchName);
    }


    public void merge(String branchName) {
        if (!branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (branchName.equals(currentBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }

        ArrayList<Integer> curList = branches.get(currentBranch);
        ArrayList<Integer> givenList = branches.get(branchName);
        int splitPoint = 0;
        for (int i = 0; i < curList.size(); i++) {
            if (curList.get(i) != givenList.get(i)) {
                splitPoint = i - 1;
                break;
            }
        }
        Node splitPointNode = commits.get(splitPoint);
        Node curBranchNode = commits.get(curList.get(curList.size() - 1));
        Node givenBranchNode = commits.get(givenList.get(givenList.size() - 1));
        HashSet<String> splitFiles = splitPointNode.commitedFiles;
        HashSet<String> curFiles = curBranchNode.commitedFiles;
        HashSet<String> givenFiles = givenBranchNode.commitedFiles;
        for (String file : givenFiles) {
            File fileFile = new File(file);

            if (!splitFiles.contains(file)) {
                if (!curFiles.contains(file)) {
                    curFiles.add(file);
                }
            } else {
                File givenStoredFile = new File(givenBranchNode.pathToPath.get(file));
                File currentStoredFile = new File(curBranchNode.pathToPath.get(file));

                int compare1 = 0;
                int compare2 = 0;
                int compare3 = 0;
                try {
                    compare1 = compareFile(givenBranchNode.pathToPath.get(file), 
                        splitPointNode.pathToPath.get(file));
                    compare2 = compareFile(curBranchNode.pathToPath.get(file), 
                        splitPointNode.pathToPath.get(file));
                } catch (IOException e) {
                    System.out.println("There are problems");
                }
                if ((compare1 == 0) && (compare2 == 1)) {
                    curBranchNode.pathToPath.put(file, givenBranchNode.pathToPath.get(file));
                } else if ((compare1 == 0) && (compare2 == 0)) {
                    File copiedConflict = new File(fileFile.getPath() + ".conflicted");
                    if (!copiedConflict.exists()) {
                        try {
                            Files.copy(givenStoredFile.toPath(), copiedConflict.toPath());
                        } catch (IOException e) {
                            System.out.println("There are problems");
                        }
                    }
                }
            }
        }
    }


    private void smallMerge(String branchName, int splitPoint) {
        ArrayList<Integer> curList = branches.get(currentBranch);
        ArrayList<Integer> givenList = branches.get(branchName);
        Node splitPointNode = commits.get(splitPoint);
        Node curBranchNode = commits.get(curList.get(curList.size() - 1));
        Node givenBranchNode = commits.get(givenList.get(givenList.size() - 1));
        HashSet<String> splitFiles = splitPointNode.commitedFiles;
        HashSet<String> curFiles = curBranchNode.commitedFiles;
        HashSet<String> givenFiles = givenBranchNode.commitedFiles;
        for (String file : givenFiles) {
            File fileFile = new File(file);

            if (!splitFiles.contains(file)) {
                if (!curFiles.contains(file)) {
                    curFiles.add(file);
                }
            } else {
                File givenStoredFile = new File(givenBranchNode.pathToPath.get(file));
                File currentStoredFile = new File(curBranchNode.pathToPath.get(file));

                int compare1 = 0;
                int compare2 = 0;
                int compare3 = 0;
                try {
                    compare1 = compareFile(givenBranchNode.pathToPath.get(file), 
                        splitPointNode.pathToPath.get(file));
                    compare2 = compareFile(curBranchNode.pathToPath.get(file), 
                        splitPointNode.pathToPath.get(file));
                } catch (IOException e) {
                    System.out.println("There are problems");
                }
                if ((compare1 == 0) && (compare2 == 1)) {
                    curBranchNode.pathToPath.put(file, givenBranchNode.pathToPath.get(file));
                }
            }
        }
    }

    public void rebase(String branchName) {
        if (!branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (branchName.equals(currentBranch)) {
            System.out.println("Cannot rebase a branch onto itself.");
            return;
        }
        ArrayList<Integer> firstlist = branches.get(branchName);
        ArrayList<Integer> secondlist = branches.get(currentBranch);
        if (secondlist.contains(firstlist.get(firstlist.size() - 1))) {
            System.out.println("Already up-to-date.");
            return;
        }
        if (firstlist.contains(secondlist.get(secondlist.size() - 1))) {
            pointer = branches.get(branchName).get((branches.get(branchName).size() - 1));
            return;
        }
        ArrayList<Integer> curList = branches.get(currentBranch);
        ArrayList<Integer> givenList = branches.get(branchName);
        int splitPoint = 0;
        for (int i = 0; i < curList.size(); i++) {
            if (curList.get(i) != givenList.get(i)) {
                splitPoint = i - 1;
                break;
            }
        }
        int index = curList.size() - 1;
        ArrayList<Node> nodes = new ArrayList<Node>();
        Node node;
        int newNodesNumber = curList.size() - (splitPoint + 1);
        for (int j = index; j > curList.indexOf(splitPoint); j--) {
            pointer = curList.get(j);
            smallMerge(branchName, splitPoint);
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            Long longTime = date.getTime();
            String formatedTime = dateFormat.format(date);
            node = new Node(id + (newNodesNumber - 1), 
                longTime, formatedTime, commits.get(pointer).message, 
                commits.get(pointer).commitedFiles, 
                commits.get(pointer).pathToPath, commits.get(pointer).branch);
            commits.put(id + (newNodesNumber - 1), node);
            nodes.add(node);
            branches.get(currentBranch).remove((Object) pointer);
            newNodesNumber -= 1;
        }
        id += newNodesNumber;
        ArrayList<Integer> newList = branches.get(branchName);
        for (int i = nodes.size() - 1; i >= 0; i--) {
            Node curnode = nodes.get(i);
            newList.add(curnode.id);
            pointer = curnode.id;
        }
        branches.put(currentBranch, newList);
    }

    public void iRebase(String branchNames) {
    }
}

//Lab3HashTable.java
// This program creates a hash table and inserts keys from an input file, with the option to choose the hash
// function or the collision technique, and calculates the minimum probes, the maximum probes, and the
// average probes and writes them to an output text file.
import java.io.*;

public class HashTableWithProbes {

    // HashNode class stores the key information, the initial hash address, the resolved hash
    // address, the amount of total probes to find the resolved address, and the
    // order in which the keys were inserted.
    static class HashNode {
        String key;
        int initialHashAddress;
        int resolvedHashAddress;
        int probes;
        int insertionOrder;

        HashNode(String key, int initialHashAddress, int resolvedHashAddress, int probes, int insertionOrder) {
            this.key = key;
            this.initialHashAddress = initialHashAddress;
            this.resolvedHashAddress = resolvedHashAddress;
            this.probes = probes;
            this.insertionOrder = insertionOrder;
        }
    }

    // Interface for hash function
    interface HashFunction {
        void hash(String key, int size, HashResult result);
    }

    // My Custom Hash Function implementation
    static class MyCustomHashFunction implements HashFunction {
        @Override
        public void hash(String key, int size, HashResult result) {
            int hashValue = 0;
            for (int i = 0; i < key.length(); i++) {
                if (i % 2 == 0) {
                    hashValue += key.charAt(i) * (i + 1); // Weighted addition for even indices
                } else {
                    hashValue -= key.charAt(i) * (i + 1); // Weighted subtraction for odd indices
                }
            }
            result.hashValue = (Math.abs(hashValue % size) +1); // Ensures range 1-100
        }
    }

    // Burris Hash Function implementation
    static class BurrisHashFunction implements HashFunction {
        @Override
        public void hash(String key, int size, HashResult result) {
            int ha = 0;
            ha += Math.abs((key.charAt(0) / 128) * 128);
            ha += (Math.abs((key.charAt(4) + key.charAt(6)) * 5) / 512);
            ha += Math.abs(key.charAt(10) / 128);

            result.hashValue = (ha % size) +1; // Ensures range 1-100
        }
    }

    static class HashResult {
        int hashValue;
    }

    // UsedIndiciesList is a linked list created to track the Used Indices for random probing
    static class UsedIndicesList {
        Node head;

        static class Node {
            int value;
            Node next;

            Node(int value) {
                this.value = value;
                this.next = null;
            }
        }

        public void add(int value) {
            Node newNode = new Node(value);
            if (head == null) {
                head = newNode;
            } else {
                Node current = head;
                while (current.next != null) {
                    current = current.next;
                }
                current.next = newNode;
            }
        }

        public boolean contains(int value) {
            Node current = head;
            while (current != null) {
                if (current.value == value) {
                    return true;
                }
                current = current.next;
            }
            return false;
        }

        public void clear() {
            head = null;
        }
    }

    // HashTable class which stores hash nodes, as well as the probing technique, false = Random
    // true = Linear. There is also the insertion count which tracks the order of insertion, as well
    //as the random seed and the Linked List which tracks the Used Indices for the random probing technique.
    static class HashTable {
        private final int size = 100;
        private HashNode[] table;
        private HashFunction hashFunction;
        private boolean useLinearProbing = true;
        private int insertionCount = 0;
        private int randomSeed = 1;
        private UsedIndicesList usedIndices = new UsedIndicesList();

        HashTable(HashFunction hashFunction) {
            this.table = new HashNode[size];
            this.hashFunction = hashFunction;
        }

        public void setProbingStrategy(boolean useLinear) {
            this.useLinearProbing = useLinear;
        }

        // generateRandomProbeIndex creates a random probe index to insert into the hash table
        // and puts that into the usedIndices list, if it is already a part of the list, it
        //regenerates another random index to be added to the list.

        private int generateRandomProbeIndex() {
            randomSeed = (5 * randomSeed) % (1 << 10);
            int index = (randomSeed / 4) % size;
            while (usedIndices.contains(index)) {
                randomSeed = (5 * randomSeed) % (1 << 10);
                index = (randomSeed / 4) % size;
            }
            usedIndices.add(index);
            return index;
        }

        // put physically puts the key into the hash table, and if there is a collision, there is a choice
        // between linear/random depending on the probing strategy currently being used
        public boolean put(String key) {
            HashResult result = new HashResult();
            hashFunction.hash(key, size, result);

            int initialHashAddress = (result.hashValue - 1) % size; // Corrected to 0-based index

            int index = initialHashAddress;
            int probes = 1;

            if (!useLinearProbing) {
                usedIndices.clear();
                usedIndices.add(initialHashAddress);
            }

            while (table[index] != null && !table[index].key.equals(key)) {
                if (useLinearProbing) {
                    index = (index + 1) % size; // Linear probing
                } else {
                    index = generateRandomProbeIndex(); // Random probing
                }

                probes++;
                if (probes > size) {
                    return false;
                }
            }

            // If the position is available or the key is found, insert it
            if (table[index] == null) {
                insertionCount++;
                // Insert the new node at the resolved index
                table[index] = new HashNode(key, initialHashAddress + 1, index + 1, probes, insertionCount); // Adjusted for 1-based display
                return true;
            }

            return false;
        }

        public void printTable(BufferedWriter writer) throws IOException {
            writer.write(String.format("%-5s %-20s %-15s %-15s %-7s %-10s%n", "Index", "Key", "Initial Hash", "Resolved Hash", "Probes", "Order"));
            for (int index = 0; index < size; index++) {
                if (table[index] != null) {
                    HashNode node = table[index];
                    writer.write(String.format("%-5d %-20s %-15d %-15d %-7d %-10d%n",
                            index + 1, node.key, node.initialHashAddress, node.resolvedHashAddress, node.probes, node.insertionOrder));
                } else {
                    writer.write(String.format("%-5d %-20s %-15s %-15s %-7s %-10s%n",
                            index + 1, "Empty", "-", "-", "-", "-"));
                }
            }
        }


        // calculateStats calculates the minimum probes for a set number of indices, the maximum probe for a set number
        public double calculateStats(int startIndex, int count, int[] minMax) {
            int totalProbes = 0;
            int entries = 0;
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;

            for (int i = 0; i < size; i++) {
                if (table[i] != null && table[i].insertionOrder >= startIndex && table[i].insertionOrder < startIndex + count) {
                    int probes = table[i].probes;
                    totalProbes += probes;
                    entries++;
                    if (probes < min) min = probes;
                    if (probes > max) max = probes;
                }
            }

            minMax[0] = min == Integer.MAX_VALUE ? 0 : min;
            minMax[1] = max == Integer.MIN_VALUE ? 0 : max;
            return entries > 0 ? (double) totalProbes / entries : 0;
        }
    }
    // Main reads input and processes the hash table, and prints it into the file "hashingOutput.txt", as
    //well as calculating the average for each parameter(first 25,last 25,all 75)
    public static void main(String[] args) {
        HashTable hashTable = new HashTable(new BurrisHashFunction());
        hashTable.setProbingStrategy(true); // true = linear, false = random

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("Words200D16.txt"));
             BufferedWriter writer = new BufferedWriter(new FileWriter("hashingOutput.txt"))) {
            String line;
            int count = 0;

            while ((line = bufferedReader.readLine()) != null && count < 75) {
                if (line.length() >= 16) {
                    String key = line.substring(0, 16);
                    hashTable.put(key);
                    count++;
                }
            }

            writer.write("Hash Table:\n");
            hashTable.printTable(writer);
            int[] minMaxFirst25 = new int[2];
            int[] minMaxLast25 = new int[2];
            int[] minMaxAll75 = new int[2];

            // Calculate stats for the first, last, and all keys
            double avgFirst25 = hashTable.calculateStats(1, 25, minMaxFirst25);
            double avgLast25 = hashTable.calculateStats(51, 25, minMaxLast25);
            double avgAll75 = hashTable.calculateStats(1, 75, minMaxAll75);

            // Write the statistics to the output file
            writer.write(String.format("Average Probes - First 25 Keys: %.2f (Min: %d, Max: %d)%n", avgFirst25, minMaxFirst25[0], minMaxFirst25[1]));
            writer.write(String.format("Average Probes - Last 25 Keys: %.2f (Min: %d, Max: %d)%n", avgLast25, minMaxLast25[0], minMaxLast25[1]));
            writer.write(String.format("Average Probes - All 75 Keys: %.2f (Min: %d, Max: %d)%n", avgAll75, minMaxAll75[0], minMaxAll75[1]));


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

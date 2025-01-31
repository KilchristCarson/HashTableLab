This Java program implements a hash table with probing techniques, designed to store and manage keys while handling hash collisions efficiently.
It includes two custom hash functions, OldHashFunction, which was a hashing function I needed to critique, and MyCustomHashFunction, which was the hash function I developed to compete with the hash function provided.
The HashTable class is the core of the implementation, which stores HashNode objects that track key details, including the initial and resolved hash addresses, the number of probes required for insertion, and the order in which the keys were added.
The program supports both linear and random probing to resolve collisions, with a linked list (UsedIndicesList) helping manage the indices used during random probing.
The main part of the program reads keys from a file (Words200D16.txt), inserts them into the hash table, and writes the resulting hash table along with the probe statistics into an output file (hashingOutput.txt).
This was a project from my Fall 2024 semester class, with some of the design choices being made due to certain restrictions I had to maintain.

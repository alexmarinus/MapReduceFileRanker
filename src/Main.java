import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;


public class Main {
	
	//Dimensiunea unui fragment si numarul de documente de parsat
	private static int D, ND;
	
	//Numele fisierelor de parsat
	private static String[] _fileNames = null;
	
	//HashMap cu cheile numele fisierelor si valorile vectori de hash-uri partiale de forma (lungime cuvant - frecventa) din fisier
	private static HashMap<String, Vector<HashMap<Integer, Integer>>> _filesToHashesFromMapTasks = 
			new HashMap<String, Vector<HashMap<Integer, Integer>>>();
	
	//HashMap cu cheile numele fisierelor si valorile numarul de cuvinte din fiecare fisier
	private static HashMap<String, Integer> _filesToWordCountsMap = 
			new HashMap<String, Integer> ();
	
	//HashMap cu cheile numele fisierelor si valorile vectori de liste de cuvinte maximale (partiale) din operatia MAP
	private static HashMap<String, Vector<ArrayList<String>>> _filesToMaximalWordsListsMapTasks = 
			new HashMap<String, Vector<ArrayList<String>>>();
	
	//HashMap cu cheile numele fisierelor si valorile hash-urile rezultate in urma operatei REDUCE
	private static HashMap<String, HashMap<Integer, Integer>> _filesToHashesFromReduceTasks = 
			new HashMap<String, HashMap<Integer, Integer>>();
	
	/*HashMap cu cheile numele fisierelor si valorile set-urile de cuvinte maximale rezultate in urma operatiei REDUCE.
	 Am ales HashSet datorita faptului ca lista finala de cuvinte maximale nu contine duplicate, proprietatea satisfacuta
	 de clasele ce implementeaza interfata Set.*/
	private static HashMap<String, HashSet<String>> _filesToMaximalWordsSetsReduceTasks = 
			new HashMap<String, HashSet<String>>();
	
	//HashMap cu cheile numele fisierelor si valorile rangurile lor
	private static HashMap<String, Double> _filesToRanks = new HashMap<String, Double>();
			
	
	/**********************************/
	
	//Determinarea indicilor de start ai fragmentelor unui fisier pe baza dimensiunii sale
	public static ArrayList<Long> getFragmentStartOffsets (long fileSize) {
		ArrayList<Long> fragmentStartOffsets = new ArrayList<Long>();
		for (long i = 0; i < fileSize; i = i + D)
			fragmentStartOffsets.add(i);
		return fragmentStartOffsets;
	}
	
	/**********************************/
	
	//Determinarea dimensiunii unui fisier cu nume specificat
	public static long getFileSize (String fileName) {
		File file = new File(fileName);
		if (!file.isFile() || !file.exists())
			return -1;
		return file.length();
	}
	
	/**********************************/
	
	
	/*Aduagarea unui hash partial rezultat dintr-o operatie MAP la intrarea corespunzatoare fisierului aferent.
	 Metoda este apelata dupa terminarea respectivei operatii MAP.*/
	public static void addHashFromFileFragment (String fileName, HashMap<Integer, Integer> partialHash) {
		Vector<HashMap<Integer, Integer>> fileHashes = _filesToHashesFromMapTasks.get(fileName);
		fileHashes.add(partialHash);
		_filesToHashesFromMapTasks.put(fileName, fileHashes);
	}
	
	/**********************************/
	
	/*Adaugarea unei liste partiale de cuvinte maximale corespunzatoare unui fragment de fisier la intrarea
	 * fisierului aferent. Metoda este apelata analog celei anterioare. */
	public static void addMaximalWordsListFromFileFragment (String fileName, ArrayList<String> maximalWordsList) {
		Vector<ArrayList<String>> fileMaximalWordsLists = _filesToMaximalWordsListsMapTasks.get(fileName);
		fileMaximalWordsLists.add(maximalWordsList);
		_filesToMaximalWordsListsMapTasks.put(fileName, fileMaximalWordsLists);
	}
	
	/**********************************/
	
	/*Adaugarea unui numar de cuvinte determinat intr-o operatie MAP. Apelare in mod analog.*/
	public static void addWordCountFromMapTask (String fileName, int wordCount) {
		int previousWordCount = _filesToWordCountsMap.get(fileName);
		_filesToWordCountsMap.put(fileName, previousWordCount + wordCount);
	}
	
	/**********************************/
	
	//Extragerea numarului de cuvinte dintr-un fisier
	public static int getWordCountForFile (String fileName) {
		int wordCount = 0;
		if (_filesToWordCountsMap.containsKey(fileName))
			wordCount = _filesToWordCountsMap.get(fileName);
		return wordCount;
	}
	
	/**********************************/
	
	//Asocierea intre un fisier si rangul calculat in operatia REDUCE. Apelare dupa terminarea operatiei.
	public static void addFileRankFromReduceTask (String fileName, Double fileRank) {
		_filesToRanks.put(fileName, fileRank);
	}
	
	/**********************************/
	
	/*Asocierea intre un fisier si setul final de cuvinte maximale rezultat din operatia REDUCE.
	Se apeleaza analog.*/
	public static void addMaximalWordsSetFromReduceTask (String fileName, HashSet<String> maximalWordsList) {
		_filesToMaximalWordsSetsReduceTasks.put(fileName, maximalWordsList);
	}
	
	/**********************************/
	
	/*Obtinerea numelor fisierlor. Metoda se apeleaza in cadrul comparatorului utilizat la
	 sortarea fisierelor conform specificatiilor cerintei.*/
	public static String[] getFileNames() {
		return _fileNames;
	}
	
	/**********************************/
	
	public static void main(String[] args) {
		int numThreads = 0;
		String inputFile = "", outputFile = "";
		if (args.length !=3) {
			System.out.println("Three arguments must be provided !");
			System.exit(-1);
		}
		try {
			numThreads = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			System.out.println("The first argument must be an integer!");
			System.exit(-1);
		}
		inputFile = args[1];
		outputFile = args[2];
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(inputFile));
			String inputFileLine = null;
			inputFileLine = bufferedReader.readLine();
			D = Integer.parseInt(inputFileLine);
			inputFileLine = bufferedReader.readLine();
			ND = Integer.parseInt(inputFileLine);
			_fileNames = new String[ND];
			for (int i = 0; i < ND; i ++) {
				inputFileLine = bufferedReader.readLine();
				_fileNames[i] = inputFileLine;
			}
		} catch (FileNotFoundException e) {
			System.out.println("The input file does not exist!");
			System.exit(-1);
		} catch (IOException e1) {
			System.out.println("IOException encountered!");
			System.exit(-1);
		}
		try {
			bufferedReader.close();
		} catch (IOException e) {
			System.out.println("The buffered reader cannot be closed!");
			System.exit(-1);
		}
		
		//Initalizare HashMap-uri
		for (int i = 0; i < ND; i++) {
			_filesToHashesFromMapTasks.put(_fileNames[i], new Vector<HashMap<Integer, Integer>>());
			_filesToMaximalWordsListsMapTasks.put(_fileNames[i], new Vector<ArrayList<String>>());
			_filesToWordCountsMap.put(_fileNames[i], 0);
			_filesToHashesFromReduceTasks.put(_fileNames[i], new HashMap<Integer, Integer>());
			_filesToRanks.put(_fileNames[i], (double) 0.00);
			_filesToMaximalWordsSetsReduceTasks.put(_fileNames[i], new HashSet<String>());
		}
		
		//WorkPool pentru operatiile de MAP
		WorkPool workPoolMap = new WorkPool(numThreads);
		ArrayList<Long> fileStartOffsetsList = null;
		//Pentru fiecare fisier
		for (int i = 0; i < ND; i ++) {
			//Determin indicii de start ai fragmentelor
			fileStartOffsetsList = getFragmentStartOffsets(getFileSize(_fileNames[i]));
			//Si pun in WorkPool task-urile de MAP corespunzatoare
			for (int j = 0; j < fileStartOffsetsList.size() ; j ++)
				workPoolMap.putWork(new MapTask(_fileNames[i], fileStartOffsetsList.get(j), D));
		}
		
		//Workerii pentru MAP - Initializare, start si join
		Worker[] mapWorkers = new Worker[numThreads];
		for (int i = 0; i < numThreads; i++)
			mapWorkers[i] = new Worker(workPoolMap);
		for (int i = 0; i < numThreads; i++)
			mapWorkers[i].start();
		for (int i = 0; i < numThreads; i++)
			try {
				mapWorkers[i].join();
			} catch (InterruptedException e) {
				System.out.format("InterruptedException for MapWorker join!");
				System.exit(-1);
			}
		
		/*In acest moment, rezulatele operatiilor MAP sunt stocate in HashMap-urile pe care 
		 le-au modificat si vor fi folosite la REDUCE.*/
		
		//WorkPool pentru operatiile de REDUCE
		WorkPool workPoolReduce = new WorkPool(numThreads);
		
		//Pentru fiecare fisier
		for (int i = 0; i < ND; i ++) {
			//Extragem numele lui, lista de hash-uri partiale si listele partiale de cuvinte maximale
			String fileName = _fileNames[i];
			Vector<HashMap<Integer, Integer>> mapHashVector = _filesToHashesFromMapTasks.get(fileName);
			Vector<ArrayList<String>> mapMaximalWordsLists = _filesToMaximalWordsListsMapTasks.get(_fileNames[i]);
			//Toate acestea se trimit unui task de REDUCE
			ReduceTask reduceTask = new ReduceTask(fileName, mapHashVector, mapMaximalWordsLists);
			//Care este pus in WorkPool
			workPoolReduce.putWork(reduceTask);
		}
			
		//Workerii de REDUCE - Initializare, start si join
		Worker[] reduceWorkers = new Worker[numThreads];
		for (int i = 0; i < numThreads; i ++)
			reduceWorkers[i] = new Worker(workPoolReduce);
		for (int i = 0; i < numThreads; i ++)
			reduceWorkers[i].start();
		for (int i = 0; i < numThreads; i ++)
			try {
				reduceWorkers[i].join();
			} catch (InterruptedException e) {
				System.out.println("InterruptedException for ReduceWorker join!");
				System.exit(-1);
			}
		
		/*In acest moment, rezultatele operatiilor de REDUCE sunt stocate in HashMap-urile pe care
		 le-au modificat. Pe baza rezultatelor fac afisarea in fisierul de output.*/
		
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;

		//Preiau setul de entry-uri din HashMap-ul de la fisiere la ranguri, deoarece pe baza lor fac afisarea
		List<Map.Entry<String, Double>> filesToRanksEntries = new ArrayList<Map.Entry<String,Double>>();
		filesToRanksEntries.addAll(_filesToRanks.entrySet());
		
		//Le sortez pe baza unui comparator definit in afara clasei Main
		Collections.sort(filesToRanksEntries,new RankComparator());
		
		try {
			fileWriter = new FileWriter(outputFile);
			bufferedWriter = new BufferedWriter(fileWriter);
			int index = 0;
			//Parcurg entry-urile sortate
			for (Map.Entry<String, Double> fileToRankEntry : filesToRanksEntries) {
				String fileName = fileToRankEntry.getKey();
				Double fileRank = fileToRankEntry.getValue();
				int maximalWordLength = _filesToMaximalWordsSetsReduceTasks.get(fileName).iterator().next().length();
				int maximalLengthFrequency = _filesToMaximalWordsSetsReduceTasks.get(fileName).size();
				bufferedWriter.write(fileName+";"+String.format("%.2f", fileRank)+";");
				bufferedWriter.write("["+maximalWordLength+","+maximalLengthFrequency+"]");
				if (index != filesToRanksEntries.size() - 1 )
					bufferedWriter.newLine();
				index ++;
			}
			bufferedWriter.close();
		} catch (IOException e) {
			System.out.println("IOException encountered at writing !");
			System.exit(-1);
		}
	}
	
}

//Clasa pentru compararea entry-urilor sortate in functie de rang
class RankComparator implements Comparator<Map.Entry<String, Double>> {
	
	@Override
	public int compare(Map.Entry<String, Double> entry1, Map.Entry<String, Double> entry2) {
		//Daca entry-urile au rang diferit, atunci cel cu rang mai mare vine primul
		if (entry2.getValue() != entry1.getValue()) {
			if (entry2.getValue() > entry1.getValue())
				return 1;
			return -1; 
		}
		//In caz contrar, se decide pe baza ordinii de aparitie a cheilor in vectorul cu numele fisierelor
		String[] fileNames = Main.getFileNames();
		int indexKey1 = Arrays.asList(fileNames).indexOf(entry1.getKey());
		int indexKey2 = Arrays.asList(fileNames).indexOf(entry2.getKey());
		if (indexKey1 > indexKey2)
			return 1;
		return -1;
	} 
}
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

//Clasa pentru operatia REDUCE
public class ReduceTask implements PartialSolution{

	//Numele fisierului
	private String _fileName;
	
	//Vector cu hash-urile partiale obtinute la operatia MAP
	private Vector<HashMap<Integer, Integer>> _mapResultHashes;
	
	//Vector cu listele de cuvinte maximale obtinute la operatia MAP
	private Vector<ArrayList<String>> _mapResultMaximalWordsLists;
	
	//Hash-ul final folosit la calculul rangului pentru fisier
	private HashMap<Integer, Integer> _reduceResultHash = new HashMap<Integer, Integer>();
	
	//Setul de cuvinte maximale ale fisierului
	private HashSet<String> _maximalWordsReducedSet = new HashSet<String>() ;
	
	//Rangul fisierului
	private double _fileRank;
	
	/**********************************/
	
	public ReduceTask(String fileName, Vector<HashMap<Integer, Integer>> mapResultHashes, Vector<ArrayList<String>> mapResultMaximalWordsLists) {
		_fileName = fileName;
		_mapResultHashes = mapResultHashes;
		_mapResultMaximalWordsLists = mapResultMaximalWordsLists;
	}
	
	/**********************************/
	
	//Executia task-ului
	@Override
	public void execute() {
		//Combinare
		combine();
		//Procesare
		process();
	}
	
	/**********************************/
	
	private void combine() {
		int wordLengthFrequency;
		// Combinarea hash-urilor
		for (int i = 0; i < _mapResultHashes.size(); i ++) {
			HashMap<Integer, Integer> wordLengthToFrequencyMap = _mapResultHashes.get(i);
			Set<Integer> wordLengths = wordLengthToFrequencyMap.keySet();
			for (Integer wordLength : wordLengths) {
				wordLengthFrequency = 0 ;
				if (_reduceResultHash.containsKey(wordLength))
					wordLengthFrequency = _reduceResultHash.get(wordLength);
				_reduceResultHash.put(wordLength, wordLengthFrequency + wordLengthToFrequencyMap.get(wordLength));
			}
		}
		
		// Combinarea listelor de cuvinte maximale
		int maximumWordLength = 0 , maximalWordLengthFromList;
		for (int i = 0; i < _mapResultMaximalWordsLists.size(); i++) {
			ArrayList<String> mapResultMaximalWordsList = _mapResultMaximalWordsLists.get(i);
			if (mapResultMaximalWordsList.isEmpty())
				continue;
			maximalWordLengthFromList = mapResultMaximalWordsList.get(0).length(); 
			if (maximalWordLengthFromList > maximumWordLength) {
				maximumWordLength = maximalWordLengthFromList;
				_maximalWordsReducedSet.clear();
				_maximalWordsReducedSet.addAll(mapResultMaximalWordsList);
			}
			else if (maximalWordLengthFromList == maximumWordLength) {
				_maximalWordsReducedSet.addAll(mapResultMaximalWordsList);
			}
		}
		
	}

	/**********************************/
	
	private void process() {
		double productSum = 0; 
		int  length = 0, frequency = 0;
		int wordCount = 0;
		for (Map.Entry<Integer, Integer> reduceResultMapEntry : _reduceResultHash.entrySet()) {
			length = reduceResultMapEntry.getKey();
			frequency = reduceResultMapEntry.getValue();
			productSum += getFibonacciNumber(length + 1) * frequency;
		}
		wordCount = Main.getWordCountForFile(_fileName);
		_fileRank = productSum / wordCount;
	}
	
	/**********************************/
	
	//Obtinerea numarului cu indexul index din sirul Fibonacci
	private int getFibonacciNumber (int index) {
		if (index == 0)
			return 0;
		if (index == 1 || index == 2)
			return 1;
		return getFibonacciNumber (index-1) + getFibonacciNumber(index-2);
	}
	
	/**********************************/
	
	//Numele fisierului
	public String getFileName() {
		return _fileName;
	}
	
	/**********************************/
	
	//Rangul fisierului
	public double getFileRank() {
		return _fileRank;
	}
	
	//Hash-ul final pentru fisier
	public HashMap<Integer, Integer> getReduceResultHash() {
		return _reduceResultHash;
	}
	
	/**********************************/
	
	//Lista finala de cuvinte maximale pentru fisier
	public HashSet<String> getMaximalWordsReducedList() {
		return _maximalWordsReducedSet;
	}
}

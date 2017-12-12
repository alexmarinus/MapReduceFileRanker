import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Clasa ce reprezinta un thread worker.
 */
class Worker extends Thread {
	WorkPool wp;
	
	public Worker(WorkPool workpool) {
		this.wp = workpool;
	}

	/**
	 * Procesarea unei solutii partiale. Aceasta poate implica generarea unor
	 * noi solutii partiale care se adauga in workpool folosind putWork().
	 * Daca s-a ajuns la o solutie finala, aceasta va fi afisata.
	 */
	void processPartialSolution(PartialSolution partialSolution) {
		String fileName = null;
		
		//Executam solutia partiala
		partialSolution.execute();
		
		//Daca este de tip Map
		if (partialSolution instanceof MapTask){
			MapTask task = (MapTask) partialSolution;
			fileName = task.getFileName();
			
			//Obtinem hash-ul partial
			HashMap<Integer, Integer> filePartialHash = task.getWordLengthToFrequencyMap();
			Main.addHashFromFileFragment(fileName, filePartialHash);
			
			//Lista de cuvinte maximale
			ArrayList<String> maximalWordsList = ((MapTask) partialSolution).getMaximalWordsList();
			Main.addMaximalWordsListFromFileFragment(fileName, maximalWordsList);
			
			//Si numarul de cuvinte
			int wordCount = ((MapTask) partialSolution).getWordCount();
			Main.addWordCountFromMapTask(fileName, wordCount);
			
			//Toate se adauga in clasa Main
			
		}
		//Daca este de tip Reduce
		else if(partialSolution instanceof ReduceTask){
			ReduceTask task = (ReduceTask) partialSolution;
			fileName = task.getFileName();
			
			//Obtinem rangul fisierului
			Double fileRank = task.getFileRank();
			Main.addFileRankFromReduceTask(fileName, fileRank);
			
			//Lista de cuvinte maximale
			HashSet<String> maximalWordsSetReduce = ((ReduceTask) partialSolution).getMaximalWordsReducedList();
			Main.addMaximalWordsSetFromReduceTask(fileName, maximalWordsSetReduce);
			
			//Toate se adauga in clasa Main
		}
	}
	
	public void run() {
		//System.out.println("Thread-ul worker " + this.getName() + " a pornit...");
		while (true) {
			PartialSolution ps = wp.getWork();
			if (ps == null)
				break;
			
			processPartialSolution(ps);
		}
		//System.out.println("Thread-ul worker " + this.getName() + " s-a terminat...");
	}
}

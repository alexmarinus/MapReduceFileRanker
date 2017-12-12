import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;

//Clasa pentru o operatie de Map
public class MapTask implements PartialSolution {
	
	//Numele fisierului, indicii de start si final ai fragmentului in cadrul fisierului
	private String _fileName;
	private long _startOffset;
	private long _endOffset;
	
	//Dimesniunea fragmentului (D sau o dimensiune mai mica)
	private long _fragmentSize;
	
	//Secventa de biti pentru fragment
	private byte[] _fragment;
	
	//Indicele de final al fisierului
	private long _fileEndOffset;
	
	//Numarul de cuvinte gasit in operatie
	private int _wordCount;
	
	//HashMap cu cheile cuvintele in lowerCase() si valorile numarul de aparitii
	private HashMap<String, Integer> _wordToFrequencyMap = new HashMap<String, Integer> ();
	
	/*HashMap cu cheile lungimile cuvintelor si cheile numarul cuvintelor cu lungimile respective
	 (hash partial)*/
	private HashMap<Integer, Integer> _wordLengthToFrequencyMap = new HashMap<Integer, Integer> ();
	
	//Lista partiala de cuvinte maximale
	private ArrayList<String> _maximalWordsList = new ArrayList<String>();
	
	//Lungimea cuvantului maximal din fragment
	private int _maximalWordLength ;
	
	/**********************************/
	
	public MapTask (String fileName, long startOffset, long fragmentSize) {
		_fileName = fileName;
		_startOffset = startOffset;
		_endOffset = _startOffset + fragmentSize - 1;
		_fragmentSize = fragmentSize;
		_fragment = null;
		_fileEndOffset = getFileSize(_fileName);
		_wordCount = 0;
		_maximalWordLength = 0;
	}
	
	/**********************************/
	
	//Dimensiunea unui fisier
	public static long getFileSize (String fileName) {
		File file = new File(fileName);
		if (!file.isFile() || !file.exists())
			return -1;
		return file.length();
	}
	
	/**********************************/
	
	//Orice caracter non-alfanumeric este separator
	private boolean isSeparatorCharacter (char character) {
		if ((character >= 'A' && character <= 'Z') || (character >= 'a' && character <= 'z'))
			return false;
		if (character >= '0' && character <= '9')
			return false;
		return true;
	}
	
	/**********************************/
	
	//Executia task-ului
	@Override
	public void execute() {
		
		//Parsarea fragmentului si modificarea HashMap-urilor
		parseFragmentAndUpdateMaps();
		//Obtinerea listei de cuvinte maximale
		computeMaximalWordsList();
	}
	
	/***********************************/
	
	private void parseFragmentAndUpdateMaps() {
		String wordToAdd = "";
		char characterRead;
		int fragmentIndex = 0, leftShifts = 0;
		RandomAccessFile randomAccessFile = null;
		try {
			
			_fileEndOffset = new File(_fileName).length();
			
			/*Daca indicele de final al fragmentului este cel putin egal cu dimensiunea fisierului,
			atunci actualizez dimensiunea fragmentului si indicele mentionat*/ 
			if (_endOffset >= _fileEndOffset) {
				_fragmentSize = (int) (_fileEndOffset - _startOffset);
				_endOffset = _fileEndOffset - 1;
			}
			
			_fragment = new byte[(int) _fragmentSize];
			
			randomAccessFile = new RandomAccessFile(new File(_fileName), "r");
			
			//Ma plasez pe _startOffset
			randomAccessFile.seek(_startOffset);
			
			/*Verific daca ma aflu in interiorul unui cuvant, deplasandu-ma la stanga cu seek
			 pana intalnesc un separator */
			characterRead = (char) randomAccessFile.read();
			while (!isSeparatorCharacter(characterRead)) {
				leftShifts ++;
				if (leftShifts > _startOffset)
					break;
				randomAccessFile.seek(_startOffset - leftShifts);
				characterRead = (char) randomAccessFile.read();
			}
			
			//Ma intorc pe _startOffset
			randomAccessFile.seek(_startOffset);
			
			//Citesc fragmentul
			randomAccessFile.read(_fragment, 0, (int) _fragmentSize);
			
			/*Daca ma deplasez mai mult de o singura data spre stanga pana la un separator,
			atunci sunt in interiorul unui cuvant*/
			boolean ignoreStartWord = (leftShifts > 1);
			
			//Daca incepe in interiorul unui cuvant, acela se ignora
			if (ignoreStartWord) {
				while (!isSeparatorCharacter((char) _fragment[fragmentIndex])) {
					fragmentIndex ++;
					if (fragmentIndex == _fragmentSize)
						return ;
				}
			}
			
			//Determinam indicele ultimului separator in cadrul fragmentului
			int lastSeparatorIndex = (int) (_fragmentSize - 1);
			while (!isSeparatorCharacter((char)_fragment[lastSeparatorIndex])) {
				lastSeparatorIndex --;
				if (lastSeparatorIndex == -1)
					break ;
			}
			
			//Parcurgem restul fragmentului
			for (int i = fragmentIndex; i <= lastSeparatorIndex; i++) {
				if (isSeparatorCharacter((char)_fragment[i])) {
					updateMapTaskUsingWord(wordToAdd.toLowerCase());
					wordToAdd = "";
					continue;
				}
				wordToAdd += (char) _fragment[i];
			}
			
			/*Daca avem un ultim separator (si deci indicele lui este diferit de -1) si diferenta
			dintre dimensiunea fragmentului si indice este mai mare decat 2, atunci fragmentul
			se termina in mijlocul unui cuvant, care va fi parsat.*/
			boolean parseEndWord = (lastSeparatorIndex != - 1) && (_fragmentSize - lastSeparatorIndex  > 2);
			
			int postFragmentIndex = (int)(_endOffset + 1);
			
			//Daca parsez si cuvantul de final
			if (parseEndWord) {
				wordToAdd = "";
				//Parcurg fragmentul de la ultimul separator pana la capat
				for (int i = lastSeparatorIndex + 1; i < _fragmentSize; i++)
					wordToAdd += (char) _fragment[i];
				//Daca nu ajung la capatul fisierului
				if (postFragmentIndex < _fileEndOffset) {
					//Ma plasez langa fragment
					randomAccessFile.seek(postFragmentIndex);
					characterRead = (char)randomAccessFile.read();
					//Pana cand intalnesc separator
					while (!isSeparatorCharacter(characterRead)) {
						wordToAdd += characterRead;
						postFragmentIndex ++;
						//Daca ajung la final de fisier, ma opresc
						if (postFragmentIndex == _fileEndOffset)
							break;
						randomAccessFile.seek(postFragmentIndex);
						characterRead = (char) randomAccessFile.read();
					}
					//Modific HashMap-urile pe baza cuvantului
					updateMapTaskUsingWord(wordToAdd.toLowerCase());
				}
				////Modific HashMap-urile pe baza cuvantului
				else 
					updateMapTaskUsingWord(wordToAdd.toLowerCase());
			}
			
		}catch (FileNotFoundException e) {
				System.out.format("IOException encountered for a fragment for %s !",_fileName);
				e.printStackTrace();
				System.exit(-1);
		} catch (IOException e) {
				System.out.format("IOException encountered with the %s filename!",_fileName);
				System.exit(-1);
		}
		
	}
	
	//Modificarea HashMap-urilor pe baza unui cuvant gasit in fragment
	private void updateMapTaskUsingWord (String wordUsed) {
		int wordFrequency = 0, wordLengthFrequency = 0;
		if (wordUsed != "" && isWord(wordUsed)) {
			_wordCount ++ ; 
			if (_wordToFrequencyMap.containsKey(wordUsed))
				wordFrequency = _wordToFrequencyMap.get(wordUsed);
			int wordLength = wordUsed.length();
			if (wordLength >= _maximalWordLength)
				_maximalWordLength = wordLength;
			if (_wordLengthToFrequencyMap.containsKey(wordLength))
				wordLengthFrequency = _wordLengthToFrequencyMap.get(wordLength);
			_wordToFrequencyMap.put(wordUsed, wordFrequency+1);
			_wordLengthToFrequencyMap.put(wordLength, wordLengthFrequency+1);
		}
	}
	
	/**********************************/

	private boolean isWord (String word) {
		return !word.matches("[-+]?\\d*\\.?\\d+");
	}
	
	/**********************************/

	//Crearea listei de cuvinte maximale
	private void computeMaximalWordsList () {
		for (String word: _wordToFrequencyMap.keySet()) {
			if (word.length() == _maximalWordLength)
				_maximalWordsList.add(word);
		}
	}
	
	/**********************************/
	
	//Numele fisierului
	public String getFileName () {
		return _fileName;
	}
	
	/**********************************/
	
	//Numarul de cuvinte
	public int getWordCount() {
		return _wordCount;
	}
	
	/**********************************/
	
	//Lungimea cuvantului maximal
	public int getMaximalWordLength() {
		return _maximalWordLength;
	}
	
	/**********************************/
	
	//HashMap-ul (cuvant - numar de aparitii)
	public HashMap<String, Integer> getWordToFrequencyMap () {
		return _wordToFrequencyMap;
	}
	
	/**********************************/
	
	//Hash-ul partial
	public HashMap<Integer, Integer> getWordLengthToFrequencyMap () {
		return _wordLengthToFrequencyMap;
	}
	
	/**********************************/
	
	//Lista de cuvinte maximale
	public ArrayList<String> getMaximalWordsList() {
		return _maximalWordsList;
	}
	
}

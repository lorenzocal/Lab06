package it.polito.tdp.meteo.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import it.polito.tdp.meteo.DAO.MeteoDAO;

public class Model {
	
	private final static int COST = 100;
	private final static int NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN = 3;
	private final static int NUMERO_GIORNI_CITTA_MAX = 6;
	private final static int NUMERO_GIORNI_TOTALI = 15;
	
	//Attributi dedicati alla ricerca dell'itinerario di costo minimo.
	private int costoMinimoAttuale;
	private List<Rilevamento> itinerarioMigliore;
	
	public int getCostoMinimoAttuale() {
		return costoMinimoAttuale;
	}

	public List<Rilevamento> getItinerarioMigliore() {
		return itinerarioMigliore;
	}

	private MeteoDAO meteoDAO;
	private List<Rilevamento> rilevamenti;

	public Model() {
		this.meteoDAO = new MeteoDAO();
		this.rilevamenti = this.meteoDAO.getAllRilevamenti();
	}
	
	private int costoItinerario(List<Rilevamento> soluzioneFinale) {
		int costo = 0;
		for (int i=0; i < soluzioneFinale.size(); i++) {
			costo = costo + soluzioneFinale.get(i).getUmidita();
			if (i > 0) {
				if (soluzioneFinale.get(i).getLocalita().compareTo(soluzioneFinale.get(i-1).getLocalita()) != 0) {
					costo = costo + Model.COST;
				}
			}
		}
		return costo;
	}
	
	private Rilevamento getRilevamentoLocalitaGiorno(List<Rilevamento> allRilevamentiMezzoMese, String localita, int giorno) {
		for (Rilevamento r : allRilevamentiMezzoMese) {
			if (r.getData().getDayOfMonth() == giorno && r.getLocalita().compareTo(localita) == 0) {
				return r;
			}
		}
		return null;
	}
	
	private boolean verificaVincoloMaxGiorni(List<Rilevamento> soluzioneParziale, String citta) {
		
		Map<String, Integer> registroRilevazioni = new HashMap<String, Integer>();
		
		for (Rilevamento r : soluzioneParziale) {
			String localita = r.getLocalita();
			if (registroRilevazioni.containsKey(localita)) {
				registroRilevazioni.put(localita, registroRilevazioni.get(localita) + 1);
			}
			else {
				registroRilevazioni.put(r.getLocalita(), 1);
			}
		}
		
		if (registroRilevazioni.get(citta) == null) {
			return true;
		}
		else {
			if (registroRilevazioni.get(citta) == Model.NUMERO_GIORNI_CITTA_MAX){
				return false;
			}
			else {
				return true;
			}
		} 
	}
	
	
	// of course you can change the String output with what you think works best
	public String getUmiditaMedia(int mese) {
		Map<String, Double> rilevamenti = meteoDAO.getUmiditaMediaMese(mese);
		String s = "";
		for (String city : rilevamenti.keySet()) {
			s = s + city + " | " + rilevamenti.get(city) + "\n";
		}
		return s;
	}
	
	
	public void creaSequenza(int mese) {
		
		this.costoMinimoAttuale = 100000;
		//Assegno un valore arbitrariamente grande al minimo attuale.
		
		this.itinerarioMigliore = new LinkedList<Rilevamento>();
		//Inizializzo la soluzione finale al problema.
		//La medesima andrà in refresh ogni volta che trovo un percorso migliore del mio migliore attuale.
		
		List<Rilevamento> allRilevamentiMezzoMese = new LinkedList<Rilevamento>();
		
		for (Rilevamento r : this.rilevamenti) {
			if (allRilevamentiMezzoMese.size() < (3 * Model.NUMERO_GIORNI_TOTALI)) {
				if (r.getData().getMonthValue() == mese && r.getData().getDayOfMonth() <= Model.NUMERO_GIORNI_TOTALI){
					allRilevamentiMezzoMese.add(r);
				}
			}
			else{
				continue;
			}
			continue;
		}
		
		List<Rilevamento> soluzioneParziale = new LinkedList<Rilevamento>();
		
		this.ricerca(soluzioneParziale, 1, allRilevamentiMezzoMese, costoMinimoAttuale);
		
		
	}
	
	private void ricerca(List<Rilevamento> soluzioneParziale, int livello, List<Rilevamento> allRilevamentiMese, int costoMinimoAttuale) {
		
		
		if (livello == Model.NUMERO_GIORNI_TOTALI + 1) { //Condizione di terminazione, ho interamente definito l'itinerario.
			
			int costoSoluzione = this.costoItinerario(soluzioneParziale);
			
			if (costoSoluzione < costoMinimoAttuale) {
				this.itinerarioMigliore.clear();
				this.itinerarioMigliore.addAll(soluzioneParziale);
				this.costoMinimoAttuale = costoSoluzione;
			}
			
		}
		
		//Nota bene che il vincolo sui 6 gg è inutile, dal momento che una città non può essere visitata più di 6 volte in 15 giorni,
		//conseguentemente ognuna delle tre città sarà visitata almeno una volta.
		
		else {
			for (Rilevamento r : allRilevamentiMese) {
				String localita = r.getLocalita();
				int giorno = r.getData().getDayOfMonth();
				if (giorno == livello) {
					if (this.verificaVincoloMaxGiorni(soluzioneParziale, localita)) {
						soluzioneParziale.add(r);
						for (int i = 1; i < Model.NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN; i++) {
							soluzioneParziale.add(this.getRilevamentoLocalitaGiorno(allRilevamentiMese, localita, livello + i));
						}
						ricerca(soluzioneParziale, livello + Model.NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN, allRilevamentiMese, this.costoMinimoAttuale);
						
						for (int i = 0; i < Model.NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN; i++) { //Backtracking per trovare tutte le soluzioni
							soluzioneParziale.remove(soluzioneParziale.size() - 1);
						}
					}				
				}
			}
		}
		
	}
	
	public List<Mese> listaMesi(){
		List<Mese> listaMesi = new LinkedList<Mese>();
		int[] numeri = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}; 
		String[] nomi = new String[] {"Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno", "Luglio", "Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre"};
		for (int i = 0; i < 12; i++) {
			listaMesi.add(new Mese(nomi[i], numeri[i]));
		}
		return listaMesi;
	}
	

}

package it.polito.tdp.meteo.model;

public class TestModel {

	public static void main(String[] args) {
		
		Model m = new Model();
		
		//System.out.println(m.getUmiditaMedia(3));
		
		//System.out.println(m.trovaSequenza(5));
		m.creaSequenza(1);
		System.out.println(m.getItinerarioMigliore() + "\n" + m.getCostoMinimoAttuale());

	}

}

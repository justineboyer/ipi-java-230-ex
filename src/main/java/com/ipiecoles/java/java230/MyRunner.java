package com.ipiecoles.java.java230;

import com.ipiecoles.java.java230.exceptions.BatchException;
import com.ipiecoles.java.java230.exceptions.TechnicienException;
import com.ipiecoles.java.java230.model.Commercial;
import com.ipiecoles.java.java230.model.Employe;
import com.ipiecoles.java.java230.model.Manager;
import com.ipiecoles.java.java230.model.Technicien;
import com.ipiecoles.java.java230.repository.EmployeRepository;
import com.ipiecoles.java.java230.repository.ManagerRepository;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class MyRunner implements CommandLineRunner {

    private static final String REGEX_MATRICULE = "^[MTC][0-9]{5}$";
    private static final String REGEX_NOM = ".*";
    private static final String REGEX_PRENOM = ".*";
    private static final int NB_CHAMPS_MANAGER = 5;
    private static final int NB_CHAMPS_TECHNICIEN = 7;
    private static final String REGEX_MATRICULE_MANAGER = "^M[0-9]{5}$";
    private static final int NB_CHAMPS_COMMERCIAL = 7;

    @Autowired
    private EmployeRepository employeRepository;

    @Autowired
    private ManagerRepository managerRepository;

    private List<Employe> employes = new ArrayList<>();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void run(String... strings) throws Exception {
        String fileName = "employes.csv";
        readFile(fileName);
        //readFile(strings[0]);
    }

    /**
     * Méthode qui lit le fichier CSV en paramètre afin d'intégrer son contenu en BDD
     * @param fileName Le nom du fichier (à mettre dans src/main/resources)
     * @return une liste contenant les employés à insérer en BDD ou null si le fichier n'a pas pu être le
     */
    public List<Employe> readFile(String fileName) {
        Stream<String> stream = null;
        //Catcher l'exeption en cas de fichiers non trouvés
        try {
        	stream = Files.lines(Paths.get(new ClassPathResource(fileName).getURI()));
        }
        catch (IOException e) {
        	logger.error("le fichier" + fileName + "n'existe pas");
        	return null;
        }
      //Afficher chaque ligne du fichier dans la console
        List<String> lignes = stream.collect(Collectors.toList());
        for(int i = 0; i < lignes.size(); i++){
            System.out.println((lignes.get(i)));
            try {
                processLine(lignes.get(i)); // Regarde la première lettre de la ligne
            } catch (BatchException e) {
                logger.error("Ligne " + (i + 1) + " : " + e.getMessage()  + " => " + lignes.get(i));
                // On passe à la ligne suivante
            }
        }

        /*logger.error("Ceci est une erreur");
        logger.warn("Ceci est un avertissement");
        logger.info("Ceci est une info");*/

        return employes;
    }

    /**
     * Méthode qui regarde le premier caractère de la ligne et appelle la bonne méthode de création d'employé
     * @param ligne la ligne à analyser
     * @throws BatchException si le type d'employé n'a pas été reconnu
     */
    private void processLine(String ligne) throws BatchException {
        //TODO
    	
    	switch (ligne.substring(0,1)) {
	    	case "T":
	    		processTechnicien(ligne);
	    		break;
	    	case "M":
	    		processManager(ligne);
	    		break;
	    	case "C":
	    		processCommercial(ligne);
	    		break;
	    	default:
	    		throw new BatchException("Type d'employé inconnu : " + ligne.substring(0,1));
    	}

    }

 
    /**
     * Méthode qui crée un Commercial à partir d'une ligne contenant les informations d'un commercial et l'ajoute dans la liste globale des employés
     * @param ligneCommercial la ligne contenant les infos du commercial à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processCommercial(String ligneCommercial) throws BatchException {
        //TODO
    	String[] champs = ligneCommercial.split(",");
    	verifChamps(champs.length,NB_CHAMPS_COMMERCIAL, "commercial");
    	String matricule = verifMatricule(champs[0], REGEX_MATRICULE);
    	LocalDate embauche = verifDate(champs[3]);
    	double salaire = salaire(champs[4]);
    	double chiffreAffaire = ca(champs[5]);
    	int performance = perf(champs[6]);
    	
    	Commercial com = new Commercial(champs[1], champs[2], matricule, embauche, salaire, chiffreAffaire, performance);
    	employeRepository.save(com);
    }
    

    /**
     * Méthode qui crée un Manager à partir d'une ligne contenant les informations d'un manager et l'ajoute dans la liste globale des employés
     * @param ligneManager la ligne contenant les infos du manager à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processManager(String ligneManager) throws BatchException {
        //TODO
    	String[]champs = ligneManager.split(",");
    	verifChamps(champs.length,NB_CHAMPS_MANAGER, "manager");
    	String matricule = verifMatricule(champs[0], REGEX_MATRICULE_MANAGER);
    	LocalDate embauche = verifDate(champs[3]);
    	double salaire = salaire(champs[4]);
    	
    	Manager mana = new Manager(champs[1], champs[2], matricule, embauche, salaire, null);
    	employeRepository.save(mana);
    }
    

    /**
     * Méthode qui crée un Technicien à partir d'une ligne contenant les informations d'un technicien et l'ajoute dans la liste globale des employés
     * @param ligneTechnicien la ligne contenant les infos du technicien à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processTechnicien(String ligneTechnicien) throws BatchException {
       
    	String[]champs = ligneTechnicien.split(",");
    	verifChamps(champs.length,NB_CHAMPS_TECHNICIEN, "technicien");
    	String matricule = verifMatricule(champs[0], REGEX_MATRICULE);
    	LocalDate embauche = verifDate(champs[3]);
    	double salaire = salaire(champs[4]);
    	int grd = grade(champs[5]);
    	String matManager = verifMatricule(champs[6], REGEX_MATRICULE_MANAGER);
    	managions(matManager);
    	
    	try {
			Technicien tech = new Technicien(champs[1], champs[2], matricule, embauche, salaire, grd);
	    	employeRepository.save(tech);
		}
		catch (TechnicienException e) {
			throw new BatchException(e.getMessage());
		}
    	
    }

	private String verifMatricule(String matricule, String reg) throws BatchException {
		if (matricule.matches(reg)) {
			return matricule;
		}
		else {
			throw new BatchException("la chaîne " + matricule + " ne respecte pas l'expression régulière " + reg);
		}	
	}
	
	private LocalDate verifDate(String date) throws BatchException {
		try {
			LocalDate d = DateTimeFormat.forPattern("dd/MM/yyyy").parseLocalDate(date);
			return d;
		}
		catch (Exception e) {
			throw new BatchException(date + " ne respecte pas le format de date dd/MM/yyyy");
		}
	}
	
	private void verifChamps(int champs, int tailleChamps, String typeEmploye) throws BatchException {
		if (champs != tailleChamps) {
			throw new BatchException("La ligne " + typeEmploye + " ne contient pas " + tailleChamps + " éléments mais " + champs);
		}
			
	}
	
	public double salaire(String salaire) throws BatchException {
		try {
			double pay = Double.parseDouble(salaire);
			return pay;
		}
		catch (Exception e) {
			throw new BatchException(salaire + " n'est pas un nombre valide pour un salaire");
		}
	}
	
	public double ca(String chiffreAffaire) throws BatchException {
		try {
			double popei = Double.parseDouble(chiffreAffaire);
			return popei;
		}
		catch (Exception e) {
			throw new BatchException("Le chiffre d'affaire du commercial est incorrect : " + chiffreAffaire );
		}
	}
	public int perf(String performance) throws BatchException {
		try {
			int perform = Integer.parseInt(performance);
			return perform;
		}
		catch (Exception e) {
			throw new BatchException("La performance du commercial est incorrecte : " + performance );
		}
	}
	public int grade(String grd) throws BatchException {
		try {
			int gr = Integer.parseInt(grd);
			return gr;
		}
		catch (Exception e) {
			throw new BatchException("Le grade du technicien est incorrect : " + grd );
		}
	}
	private void managions(String matricule) throws BatchException {
		Employe e = employeRepository.findByMatricule(matricule);
		if (e == null) {
			throw new BatchException("Le manager de matricule " + matricule + " n'a pas été trouvé dans le fichier ou en base de données");
		}
			
	}
	

}

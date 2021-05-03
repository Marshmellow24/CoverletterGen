package cbr;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import application.CL_GeneratorUtils;
import de.dfki.mycbr.core.DefaultCaseBase;
import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.casebase.Attribute;
import de.dfki.mycbr.core.casebase.Instance;
import de.dfki.mycbr.core.casebase.MultipleAttribute;
import de.dfki.mycbr.core.model.AttributeDesc;
import de.dfki.mycbr.core.model.BooleanDesc;
import de.dfki.mycbr.core.model.Concept;
import de.dfki.mycbr.core.model.ConceptDesc;
import de.dfki.mycbr.core.model.StringDesc;
import de.dfki.mycbr.core.model.SymbolDesc;
import de.dfki.mycbr.core.retrieval.Retrieval;
import de.dfki.mycbr.core.retrieval.Retrieval.RetrievalMethod;
import de.dfki.mycbr.core.similarity.AmalgamationFct;
import de.dfki.mycbr.core.similarity.ISimFct;
import de.dfki.mycbr.core.similarity.Similarity;
import de.dfki.mycbr.core.similarity.StringFct;
import de.dfki.mycbr.core.similarity.config.AmalgamationConfig;
import de.dfki.mycbr.core.similarity.config.MultipleConfig;
import de.dfki.mycbr.core.similarity.config.MultipleConfig.MainType;
import de.dfki.mycbr.core.similarity.config.MultipleConfig.Reuse;
import de.dfki.mycbr.core.similarity.config.MultipleConfig.Type;
import de.dfki.mycbr.core.similarity.config.StringConfig;
import de.dfki.mycbr.io.CSVImporter;
import de.dfki.mycbr.util.Pair;

public class CBR_retrieval {
	private final String data_path = "cb/"; 
	private final String csv = "cv_casebase.csv"; 
	
	private final String columnseparator = ";"; 
	private final String multiplevalueseparator = ","; 
	
	private Project p;
	private Concept concept;
	private DefaultCaseBase cb;
	private CSVImporter csvImporter;
	private Retrieval ret;
	private Instance query;
	
	private StringDesc expDesc;
	private StringDesc jobDesc;
	private StringDesc branchDesc;
	private StringDesc skillDesc;
	private StringDesc langDesc;
	private StringDesc degDesc;
	private StringDesc titlDesc;
	private SymbolDesc charDesc;
	private SymbolDesc driversDesc;
	
	public CBR_retrieval(String conceptName, String cbName) throws Exception {
		// setup project and assign concept, casebase, csvimporter
		this.p = new Project();
		this.concept = this.p.createTopConcept(conceptName);
		this.cb = this.p.createDefaultCB(cbName);
		this.csvImporter = new CSVImporter(this.data_path+this.csv, this.concept);
	}
	
	public void setupCB() throws InterruptedException {
		// setup csv importer class and import data
		this.csvImporter.setCaseBase(this.cb);
		this.csvImporter.setSeparator(this.columnseparator);
		this.csvImporter.setSeparatorMultiple(this.multiplevalueseparator);
		this.csvImporter.setSymbolThreshold(2);
		this.csvImporter.readData();	
		this.csvImporter.checkData();
		this.csvImporter.addMissingValues();
		this.csvImporter.addMissingDescriptions();			
		this.csvImporter.doImport();
		System.out.println("[CBR-Engine] CaseBase setup suceeded!");
		Thread.sleep(1500); 
	}
	
	public void setupRetrieval() {
		// set up retrieval and query instance and assign to concept and cb
		this.ret = new Retrieval(this.concept, this.cb);
		this.ret.setRetrievalMethod(RetrievalMethod.RETRIEVE_SORTED);
		this.query = this.ret.getQueryInstance();
	}
	
	public void setupAttributes() throws Exception {
		// assign descriptions
		this.skillDesc = (StringDesc) this.concept.getAllAttributeDescs().get("Skills");
		this.jobDesc = (StringDesc) this.concept.getAllAttributeDescs().get("Current Job");
		this.branchDesc = (StringDesc) this.concept.getAllAttributeDescs().get("Branch");
		this.expDesc = (StringDesc) this.concept.getAllAttributeDescs().get("Experience");
		this.langDesc = (StringDesc) this.concept.getAllAttributeDescs().get("Languages");
		this.degDesc = (StringDesc) this.concept.getAllAttributeDescs().get("Degrees");
		this.titlDesc = (StringDesc) this.concept.getAllAttributeDescs().get("Titles");
		this.charDesc = (SymbolDesc) this.concept.getAllAttributeDescs().get("Charity");
		this.driversDesc = (SymbolDesc) this.concept.getAllAttributeDescs().get("Drivers License");
		this.concept.removeAttributeDesc("Coverletter Code");
		
		// set string descriptions to multiple values 
		this.skillDesc.setMultiple(true);
		this.jobDesc.setMultiple(true);
		this.branchDesc.setMultiple(true);
		this.expDesc.setMultiple(true);
		this.langDesc.setMultiple(true);
		this.degDesc.setMultiple(true);
		this.titlDesc.setMultiple(true);
		
		// assign new amalgam function 
		AmalgamationFct amalgam = this.concept.addAmalgamationFct(AmalgamationConfig.WEIGHTED_SUM, "innerF", true);	
		
		// add string fct to string attribute descriptions
		StringFct s1 = this.skillDesc.addStringFct(StringConfig.LEVENSHTEIN, "skillf", true);
		StringFct s2 = this.jobDesc.addStringFct(StringConfig.LEVENSHTEIN, "jobf", true);
		StringFct s3 = this.branchDesc.addStringFct(StringConfig.LEVENSHTEIN, "branchf", true);
		StringFct s4 = this.expDesc.addStringFct(StringConfig.LEVENSHTEIN, "expf", true);
		StringFct s5 = this.langDesc.addStringFct(StringConfig.LEVENSHTEIN, "langf", true);
		StringFct s6 = this.degDesc.addStringFct(StringConfig.LEVENSHTEIN, "degf", true);
		StringFct s7 = this.titlDesc.addStringFct(StringConfig.LEVENSHTEIN, "titlf", true);
		
		// turn off case sensitivity for string attribute functions
		s1.setCaseSensitive(false);
		s2.setCaseSensitive(false);
		s3.setCaseSensitive(false);
		s4.setCaseSensitive(false);
		s5.setCaseSensitive(false);
		s6.setCaseSensitive(false);
		s7.setCaseSensitive(false);
		
		// assign multiple configs for multiple attributes for string attributes
		s1.setMultipleConfig(new MultipleConfig(MainType.PARTNER_QUERY, Reuse.REUSE, Type.AVG));
		s2.setMultipleConfig(new MultipleConfig(MainType.PARTNER_QUERY, Reuse.REUSE, Type.AVG));
		s3.setMultipleConfig(new MultipleConfig(MainType.PARTNER_QUERY, Reuse.REUSE, Type.AVG));
		s4.setMultipleConfig(new MultipleConfig(MainType.PARTNER_QUERY, Reuse.REUSE, Type.AVG));
		s5.setMultipleConfig(new MultipleConfig(MainType.PARTNER_QUERY, Reuse.REUSE, Type.AVG));
		s6.setMultipleConfig(new MultipleConfig(MainType.PARTNER_QUERY, Reuse.REUSE, Type.AVG));
		s7.setMultipleConfig(new MultipleConfig(MainType.PARTNER_QUERY, Reuse.REUSE, Type.AVG));
		
		// assure that correct sim functions are active
		amalgam.setActiveFct(this.skillDesc, s1);
		amalgam.setActiveFct(this.jobDesc, s2);
		amalgam.setActiveFct(this.branchDesc, s3);
		amalgam.setActiveFct(this.expDesc, s4);
		amalgam.setActiveFct(this.langDesc, s5);
		amalgam.setActiveFct(this.degDesc, s6);
		amalgam.setActiveFct(this.titlDesc, s7);
		
		// assign weights and create retrieval and query instance
		this.setWeights();
		this.setupRetrieval();
		
		
		System.out.println("[CBR-Engine] Attribute Descriptions is set up and retrieval possible!");
		
	}
	
	public void setWeights() {
		// set the weights statically
		this.concept.getActiveAmalgamFct().setWeight(this.expDesc, 3.0);
		this.concept.getActiveAmalgamFct().setWeight(this.jobDesc, 1.0);
		this.concept.getActiveAmalgamFct().setWeight(this.branchDesc, 2.0);
		this.concept.getActiveAmalgamFct().setWeight(this.skillDesc, 3.0);
		this.concept.getActiveAmalgamFct().setWeight(this.langDesc, 1.0);
		this.concept.getActiveAmalgamFct().setWeight(this.degDesc, 2.0);
		this.concept.getActiveAmalgamFct().setWeight(this.charDesc, 1.0);
		this.concept.getActiveAmalgamFct().setWeight(this.driversDesc, 2.0);
		System.out.println("[CBR-Engine] Weights are set accordingly!");
	}
	
	public List<Pair<Instance, Similarity>> runRetrieval() {
		HashMap<Instance,Double> resultMap = new HashMap<Instance,Double>();
		// run retrieval)
		this.ret.start();
		// wait until finished
		while (!this.ret.isFinished()) {
	
		}
		// assign retrieval results
		List<Pair<Instance, Similarity>> result = this.ret.getResult();
		// print out results
		for (int i = 0; i < 3; i++) {
			System.out.println(result.get(i).getSecond());
			
			System.out.println(result.get(i).getFirst());
		}
		return result;
	}

	public void createQuery(HashMap<String,ArrayList<String>> parsedCV) throws Exception {
		// take HashMap from parsed JSON file and assign to query attributes
		for (Map.Entry<String, ArrayList<String>> input: parsedCV.entrySet()) {
			switch(input.getKey()) {
			case "Current Job":
				// if array list is empty 
				if (input.getValue().isEmpty()) {
					System.out.println("[Query] No entry found for " + this.jobDesc.getName());
					break;
				}
				System.out.println("[Query] Found Current Job entry!");
				// create linkedlist to append the values from the arraylist to it (multipleattribute only takes this class as parameter)
				LinkedList<Attribute> listJob = new LinkedList<Attribute>();
				for (String temp: input.getValue()) {
					// transform the String to attributes
					listJob.add(this.jobDesc.getAttribute(temp));
				}
				// merge all the attributes from linkedlist to one multipleattribute object and assign to attribute description
				MultipleAttribute<StringDesc> multJob = new MultipleAttribute<StringDesc>(this.jobDesc, listJob); 
				// add attributes to query
				System.out.println("Added " + multJob + " successfully: " + this.query.addAttribute(this.jobDesc.getName(), multJob));
				break;
				
			case "Experience":
				if (input.getValue().isEmpty()) {
					System.out.println("[Query] No entry found for " + this.expDesc.getName());
					break;
				}
				System.out.println("[Query] Found Experience entry!");
				LinkedList<Attribute> listExp = new LinkedList<Attribute>();
				for (String temp: input.getValue()) {
					listExp.add(this.expDesc.getAttribute(temp));
				}
				MultipleAttribute<StringDesc> multExp = new MultipleAttribute<StringDesc>(this.expDesc, listExp); 
				System.out.println("Added " + multExp + " successfully: " + this.query.addAttribute(this.expDesc.getName(), multExp));
				break;
				
			case "Skills":
				if (input.getValue().isEmpty()) {
					System.out.println("[Query] No entry found for " + this.skillDesc.getName());
					break;
				}
				System.out.println("[Query] Found Skills entry!");
				LinkedList<Attribute> listSkill = new LinkedList<Attribute>();
				for (String temp: input.getValue()) {
					listSkill.add(this.skillDesc.getAttribute(temp));
				}
				MultipleAttribute<StringDesc> multSkill = new MultipleAttribute<StringDesc>(this.skillDesc, listSkill); 
				System.out.println("Added " + multSkill + " successfully: " + this.query.addAttribute(this.skillDesc.getName(), multSkill));
				break;
				
			case "Charity":
				if (input.getValue().isEmpty()) {
					System.out.println("[Query] No entry found for " + this.charDesc.getName());
					break;
				}
				System.out.println("[Query] Found Charity entry!");
				// create boolean from string
				boolean charityVal = false;
				if (input.getValue().size() > 1) {
					charityVal=false;
					System.out.println("Query contains more than one value! Please make sure Charity contains only one boolean value. Value will be set to unknown.");
					break;
				} else if(input.getValue().get(0)=="true") {
					charityVal=true;
				} else if(input.getValue().get(0)=="false") {
					charityVal=false;
				}
				System.out.println("Added " + input.getValue().get(0) + ": " + this.query.addAttribute(this.charDesc, this.charDesc.getAttribute(charityVal)));
				break;
				
			case "Drivers License":
				if (input.getValue().isEmpty()) {
					System.out.println("[Query] No entry found for " + this.driversDesc.getName());
					break;
				}
				System.out.println("[Query] Found Drivers License entry!");
				boolean driversVal = false;
				if (input.getValue().size() > 1) {
					driversVal=false;
					System.out.println("Query contains more than one value! Please make sure Drivers License contains only one boolean value. Value will be set to unknown.");
					break;
				} else if(input.getValue().get(0)=="true") {
					driversVal=true;
				} else if(input.getValue().get(0)=="false") {
					driversVal=false;
				}
				System.out.println("Added " + input.getValue().get(0) + ": " + this.query.addAttribute(this.driversDesc, this.driversDesc.getAttribute(driversVal)));
				break;
				
			case "Branch":
				if (input.getValue().isEmpty()) {
					System.out.println("[Query] No entry found for " + this.branchDesc.getName());
					break;
				}
				System.out.println("[Query] Found Branch entry!");
				LinkedList<Attribute> listBranch = new LinkedList<Attribute>();
				for (String temp: input.getValue()) {
					listBranch.add(this.branchDesc.getAttribute(temp));
				}
				MultipleAttribute<StringDesc> multBranch = new MultipleAttribute<StringDesc>(this.branchDesc, listBranch); 
				System.out.println("Added " + multBranch + " successfully: " + this.query.addAttribute(this.branchDesc.getName(), multBranch));
				break;
				
			case "Degrees":
				if (input.getValue().isEmpty()) {
					System.out.println("[Query] No entry found for " + this.degDesc.getName());
					break;
				}				
				System.out.println("[Query] Found Degrees entry!");
				LinkedList<Attribute> listDeg = new LinkedList<Attribute>();
				for (String temp: input.getValue()) {
					listDeg.add(this.degDesc.getAttribute(temp));
				}
				MultipleAttribute<StringDesc> multDeg = new MultipleAttribute<StringDesc>(this.degDesc, listDeg); 
				System.out.println("Added " + multDeg + " successfully: " + this.query.addAttribute(this.degDesc.getName(), multDeg));
				break;
				
			case "Languages":
				if (input.getValue().isEmpty()) {
					System.out.println("[Query] No entry found for " + this.langDesc.getName());
					break;
				}		
				System.out.println("[Query] Found Languages entry!");
				LinkedList<Attribute> listLang = new LinkedList<Attribute>();
				for (String temp: input.getValue()) {
					listLang.add(this.langDesc.getAttribute(temp));
				}
				MultipleAttribute<StringDesc> multLang = new MultipleAttribute<StringDesc>(this.langDesc, listLang); 
				System.out.println("Added " + multLang + " successfully: " + this.query.addAttribute(this.langDesc.getName(), multLang));
				break;
				
			case "Titles":
				if (input.getValue().isEmpty()) {
					System.out.println("[Query] No entry found for " + this.titlDesc.getName());
					break;
				}		
				System.out.println("[Query] Found Titles entry!");
				LinkedList<Attribute> listTitl = new LinkedList<Attribute>();
				for (String temp: input.getValue()) {
					listTitl.add(this.titlDesc.getAttribute(temp));
				}
				MultipleAttribute<StringDesc> multTitl = new MultipleAttribute<StringDesc>(this.titlDesc, listTitl); 
				System.out.println("Added " + multTitl + " successfully: " + this.query.addAttribute(this.titlDesc.getName(), multTitl));
				break;
			}
		}

		System.out.println("\n" + this.query.getAttributes());
		
	}
	
	
	public static void main(String[] args) {
//		----------------------------------------------Test Query-------------------------------------------
		try {
			boolean DEBUG = false;
			String skill_data = "Projekttools Testtools TroubleTicketSystem Versionsverwaltung,Linux,Unix,MS Dos,Windows-Derivate,IBM Cognos Report Studio,Analysis Framework,Apache Hadoop,Access,MySQL,Oracle,Lotus Notes Client,MS-Outlook,Confluence,Puppet,PL Visual Basic,Java,C #,Perl,HTML,CSS,JavaScript,Mindmanager,Office,Project,Quality Center ALM,TestLink,EnterpriseTester,SpiraTest,Selenium IDE Server,Zephyr for JIRA,synapseRT HP OpenView Desk,BMC Remedy AR System,OmniNet OmniTracker,GIT,Subversion,CA Software Change Workbench Standard Basis Projekterfahrung Auswahl Zeitraum Branche T�tigkeit Projektbeschreibung,LBS";
			String exp_data = "Softwaretest -testmanagement,Projektmanagement,IT Service Management,Releasemanagement,Business Intelligence,Certified Tester Foundation Level ISTQB Test Manager,ITIL � V3 Fachmann IPMA GPM Branchenschwerpunkte,Banken,Bausparkassen,Finanzdienstleister,IT-Dienstleister Einzelhandel Automatisierung Testdurchf�hrung Prozessoptimierung Defect Konzeption Implementierung Rahmen Abl�sung Logistik-Systems Java-Middleware Disposition Lagerhaltung Lager Mitglied Testcenters Review-Prozess,Finanz-Dienstleister Programmierung,Buchhandel Release Koordinierung Releases eCommerce-Eigenentwicklung Optimierung Deploymentprozesses beratung,System-Release Ausf�hrung,Testkoordination Vorstand,IT-Dienstleisters,Transition Gro�bank,1,2,3,4,5,6,7,8,9,10,Integration Integrationstest,Selection Hardware Entscheidungsunterst�tzungssystem Hardware-Beschaffung Lebenszykluskosten";
			
			ArrayList<String>skills_split = new ArrayList<String>();
			ArrayList<String>exp_split = new ArrayList<String>();
			
			for (String skill: skill_data.split(",")) {
				skills_split.add(skill);
				
			}
			for (String exp: exp_data.split(",")) {
				exp_split.add(exp);
				
			}
			
			// hashmap that contains attribute desciption with according attributes that should be retrieved as array list
			HashMap<String,ArrayList<String>> queryHash = new HashMap<String,ArrayList<String>>();
			
			// assign arraylists to enable multiple attributes per description and outsource retrieval function
			ArrayList<String> expQuery = new ArrayList<String>();
			expQuery.add("Releasemanagement");
			expQuery.add("Business Intelligence");
			expQuery.add("Projektmanagement");
			queryHash.put("Experience", expQuery);
			
			ArrayList<String> jobQuery = new ArrayList<String>();
			jobQuery.add("IT-Dienstleistung");
			jobQuery.add("Projektleitung");
			
			queryHash.put("Current Job", jobQuery);
			
			ArrayList<String> branchQuery = new ArrayList<String>();
			branchQuery.add("IT");
			queryHash.put("Branch", branchQuery);
			
			ArrayList<String> skillQuery = new ArrayList<String>();
			skillQuery.add("Apache Hadoop");
			queryHash.put("Skills", skillQuery);
		
			ArrayList<String> langQuery = new ArrayList<String>();
			langQuery.add("English");
			queryHash.put("Languages", langQuery);
			
			ArrayList<String> degQuery = new ArrayList<String>();
			degQuery.add("Bachelor");
			queryHash.put("Degrees", degQuery);
			
			ArrayList<String> titlQuery = new ArrayList<String>();
			titlQuery.add("Dr");
			queryHash.put("Titles", titlQuery);
			
			ArrayList<String> charQuery = new ArrayList<String>();
			charQuery.add("true");
			queryHash.put("Charity", charQuery);
			
			ArrayList<String> driversQuery = new ArrayList<String>();
			driversQuery.add("true");
			queryHash.put("Drivers License", driversQuery);
			
			CBR_retrieval test = new CBR_retrieval("cv", "test_cb");
			
			if (!DEBUG) {
//				test.setupCB();
//				test.setupAttributes();
//				
//				test.createQuery(queryHash);
//				test.runRetrieval();
//				
				test.setupCB();
				test.setupAttributes();
				
				test.createQuery(CL_GeneratorUtils.JSONtoMap("test_cv_parsed.json"));
				test.runRetrieval();
					
			}
//			
			// show attributes and descs of query
//			for(Map.Entry<AttributeDesc, ArrayList<String>> entry: queryHash.entrySet()) {
//				System.out.println(entry.getKey().getName() + "\t" + entry.getValue());
//			}
			
//			Iterator it = anschreiben.getAllAttributeDescs().entrySet().iterator();
//			
//			while (it.hasNext()) {
//				Map.Entry pair = (Map.Entry)it.next();
//				System.out.println("Attribute: " + pair.getKey() + " Type: " + pair.getValue().getClass().getSimpleName());
//			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		
	}

}

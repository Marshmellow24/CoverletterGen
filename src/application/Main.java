package application;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.json.simple.parser.ParseException;

import cbr.CBR_retrieval;
import de.dfki.mycbr.core.casebase.Instance;
import de.dfki.mycbr.core.retrieval.Retrieval;
import de.dfki.mycbr.core.similarity.Similarity;
import de.dfki.mycbr.util.Pair;

public class Main extends JFrame {
    private final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);  
    private FileHandler fh;  
	
	private File cv;
	private File coverletterFile;
	
	private String parsedJSON;
	private HashMap<String,ArrayList<String>> mappedJSON;
	private HashMap<String,String> userInputMap;
	private List<Pair<Instance, Similarity>> result;
	private String paraphrasedCLOutput;
	private String paraphrasedCLContent;
	
	private JTextArea cvTextArea;
	private StringBuilder textAreaContent;
	
	private JMenuItem retrieve;
	private JMenuItem load;
	private JMenuItem save;
	private JMenuItem generate;
	private JMenuItem help;
	
	private JTextField nameField;
	private JTextField streetField;
	private JTextField houseNumField;
	private JTextField zip_code;
	private JTextField town;	
	private JTextField salary;
	private JTextField entryDate;
	
	private JTextField jobNameField;
	private JTextField jobStreetField;
	private JTextField jobHouseNumField;
	private JTextField job_zip_code;
	private JTextField jobTown;	
	private JTextField companyNameField;
	
	private ArrayList<JTextField> input_fields;

	public Main() {
		// frame settings
		setTitle("Coverletter Generator");
		setSize(800, 1000);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		// Main container
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(2, 1));
		
		// Top container with text area
		JPanel textPanel = new JPanel();
		textPanel.setLayout(new BorderLayout());
		
		// bottom container with text fields for input
		JPanel botPanel = new JPanel();
		JPanel botSubPanelTop = new JPanel();
		JPanel botSubPanelTopTitle = new JPanel();
		JPanel botSubPanelBottom = new JPanel();
		
		botPanel.setLayout(new GridLayout(2, 1));
		botPanel.setBorder(new EtchedBorder());

		botSubPanelTopTitle.setLayout(new FlowLayout(FlowLayout.CENTER, 200, 0));
		botSubPanelTop.setLayout(new BoxLayout(botSubPanelTop, BoxLayout.Y_AXIS));
		
		botSubPanelBottom.setLayout(new BoxLayout(botSubPanelBottom, BoxLayout.Y_AXIS));
		
		// scrollbar
		JScrollPane scroll = new JScrollPane(textPanel,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		
		// content TopPanel loaded_cv textarea 
		this.cvTextArea = new JTextArea("[Coverletter Generator] Bitte Anschreiben auswählen (JSON oder PDF)");
		this.cvTextArea.setBorder(new TitledBorder(new EtchedBorder(), "Anzeige"));
		this.cvTextArea.setLineWrap(true);
		this.cvTextArea.setEditable(false);
		
		// menu bar
		JMenuBar menu = new JMenuBar();
		JMenu datei = new JMenu("Datei");
		this.load = new JMenuItem("Lebenslauf laden");
		this.save = new JMenuItem("Generiertes Anschreiben speichern");
		this.retrieve = new JMenuItem("Daten aus Lebenslauf extrahieren und Retrieval durchführen");
		this.generate = new JMenuItem("Anschreiben aus abgerufenem Fall generieren");
		this.help = new JMenuItem("Hilfe");
		
		datei.add(this.load);
		datei.add(this.retrieve);
		datei.add(this.generate);
		datei.add(this.save);
		datei.add(this.help);
		menu.add(datei);
		this.setJMenuBar(menu);
		
		// buttons main panel
		JButton userTemplateButton = new JButton("Beispieleingaben erstellen");
		this.retrieve.setEnabled(false);
		this.generate.setEnabled(false);
		this.save.setEnabled(false);

		
		// select file
		this.load.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				loadCV();
			}
		});
		
		// save generated coverletter
		this.save.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				saveCoverletter();
			}
		});
		
		// retrieve case
		this.retrieve.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					extractCV();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		// generate cover letter 
		this.generate.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				generateCoverletter();
			}
		});
		
		// open help dialog
		this.help.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				helpDialog();
			}
		});
		
		// add template input
		userTemplateButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				templateInput();
			}
		});
		
		// content BottomPanel user data	
		JLabel userLabel = new JLabel("Bitte Daten des Nutzers angeben");
		this.nameField = new JTextField("Vorname Nachname");
		this.nameField.setName("Name");
		
		this.streetField = new JTextField("Straße");
		this.streetField.setName("Street");
		
		this.houseNumField = new JTextField("Nr");
		this.houseNumField.setName("Street Number");
		
		this.zip_code = new JTextField("PLZ");
		this.zip_code.setName("ZIP");
		
		this.town = new JTextField("Ort");
		this.town.setName("Town");
		
		this.salary = new JTextField("Gehaltswunsch");
		this.salary.setName("Salary");
		
		this.entryDate = new JTextField("Frühestes Eintrittsdatum");
		this.entryDate.setName("Entry Date");
		
		JLabel jobLabel = new JLabel("Bitte Name und Anschrift des Unternehmens angeben");
		this.companyNameField = new JTextField("Name des Unternehmens");
		this.companyNameField.setName("Company");
		
		this.jobNameField = new JTextField("Name der Tätigkeit");
		this.jobNameField.setName("Job Title");
		
		this.jobStreetField = new JTextField("Straße");
		this.jobStreetField.setName("Job Street Name");
		
		this.jobHouseNumField = new JTextField("Nr");
		this.jobHouseNumField.setName("Job Street Number");
		
		this.job_zip_code = new JTextField("PLZ");
		this.job_zip_code.setName("Job ZIP");
		
		this.jobTown = new JTextField("Ort");
		this.jobTown.setName("Job Town");
		
		// focus listener for textfields
		this.input_fields = new ArrayList<JTextField>();
	
		this.input_fields.add(this.nameField);          
		this.input_fields.add(this.streetField);        
		this.input_fields.add(this.houseNumField);      
		this.input_fields.add(this.zip_code);           
		this.input_fields.add(this.town);	           
		this.input_fields.add(this.jobNameField);       
		this.input_fields.add(this.jobStreetField);     
		this.input_fields.add(this.jobHouseNumField);   
		this.input_fields.add(this.job_zip_code);       
		this.input_fields.add(this.jobTown);	           
		this.input_fields.add(this.companyNameField);  
		this.input_fields.add(this.salary);
		this.input_fields.add(this.entryDate);
		
		for (JTextField jtf: this.input_fields) {
			String placeholder = jtf.getText();
			jtf.setForeground(Color.GRAY);
			jtf.addFocusListener(new FocusListener() {
				@Override
				public void focusLost(FocusEvent e) {
					 if (jtf.getText().isEmpty()) {
						 jtf.setForeground(Color.GRAY);
						 jtf.setText(placeholder);
				        }
				}
				
				@Override
				public void focusGained(FocusEvent e) {
			        if (jtf.getText().equals(placeholder)) {
			        	jtf.setText("");
			        	jtf.setForeground(Color.BLACK);
			        }
				}
			});
		}
		
		// panel logic
		this.add(mainPanel);
		
		mainPanel.add(scroll);
		mainPanel.add(botPanel);
		
		textPanel.add(this.cvTextArea);
		
		botPanel.add(botSubPanelTop);
		botPanel.add(botSubPanelBottom);
		
		botSubPanelTop.add(botSubPanelTopTitle);
		botSubPanelTopTitle.add(userLabel);
		botSubPanelTopTitle.add(userTemplateButton);
		
		botSubPanelTop.add(this.nameField);
		botSubPanelTop.add(this.streetField);
		botSubPanelTop.add(this.houseNumField);
		botSubPanelTop.add(this.zip_code);
		botSubPanelTop.add(this.town);
		botSubPanelTop.add(this.salary);
		botSubPanelTop.add(this.entryDate);
		
		botSubPanelBottom.add(jobLabel);
		botSubPanelBottom.add(this.jobNameField);
		botSubPanelBottom.add(this.companyNameField);
		botSubPanelBottom.add(this.jobStreetField);
		botSubPanelBottom.add(this.jobHouseNumField);
		botSubPanelBottom.add(this.job_zip_code);
		botSubPanelBottom.add(this.jobTown);
		
		try {
			this.logger.setLevel(Level.INFO);
			this.logger.setUseParentHandlers(false);
			this.fh = new FileHandler(".\\.\\.\\logs\\log.txt", true);
			this.logger.addHandler(this.fh);
			SimpleFormatter formatter = new SimpleFormatter();  
	        this.fh.setFormatter(formatter);  
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void loadCV() {
		// String Builder for text area content
		this.textAreaContent = new StringBuilder();
		JFileChooser loadCVFileChooser = new JFileChooser();
		
		// only accept PDF format
		loadCVFileChooser.setAcceptAllFileFilterUsed(false);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("CV files", "pdf", "json");
		loadCVFileChooser.addChoosableFileFilter(filter);

		
		// pop up open file dialog and select PDF fole 
		if (loadCVFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			this.cv = loadCVFileChooser.getSelectedFile();
			
			this.textAreaContent.append("[Coverletter Generator] Filename: " + loadCVFileChooser.getSelectedFile().getName());
			this.textAreaContent.append("\n[Coverletter Generator] Path: " + this.cv.getPath() + "\n");
			this.cvTextArea.setText(this.textAreaContent.toString());
			
			// set menu items editable / non editable
			this.retrieve.setEnabled(true);
			this.deactivateMenuItems();
		} else {
			// user clicks on cancel or selects non valid file
			System.out.println("No valid file selected");
		}
	}
	
	private void saveCoverletter() {
		//set up filechooser, show only directories
		JFileChooser saveCL = new JFileChooser();
		saveCL.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		if (saveCL.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			// assign selected directory path to class attribute
			this.coverletterFile = saveCL.getSelectedFile();
			
			try {
				// concatenate fileName and path
				String saveFilePath = this.coverletterFile.toString() + "\\" 
						+ this.companyNameField.getText().replaceAll(" ", "") + "_" 
						+ this.nameField.getText().replaceAll(" ", "") + "_anschreiben.txt";
				System.out.println(saveFilePath);
				// write to file 
				PrintWriter out = new PrintWriter(new File(saveFilePath));
				
				// write header to file using input data 
				out.println(this.companyNameField.getText());
				out.println(this.jobStreetField.getText() + " " + this.jobHouseNumField.getText());
				out.println(this.job_zip_code.getText() + " " + this.jobTown.getText());
				out.println("\n");
				out.println(this.nameField.getText());
				out.println(this.streetField.getText() + " " + this.houseNumField.getText());
				out.println(this.zip_code.getText() + " " + this.town.getText());
				out.println("\n");
				out.println("Bewerbung als " + this.jobNameField.getText() + "\n");
				out.println("Sehr geehrte Damen und Herren,\n");
				// write generated coverletter content to file
				out.print(this.paraphrasedCLContent);
				// add regards
				out.println("\nMit freundlichen Grüßen,\n" + this.nameField.getText());
				
				out.close();
				// show file path in text area
				this.textAreaContent.append("\n[Coverletter Generator] Anschreiben gespeichert in: " + saveFilePath);
				this.cvTextArea.setText(this.textAreaContent.toString());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("No valid file selected");
		}
	}
	
	private void deactivateMenuItems() {
		// preserve user journey
		if (this.generate.isEnabled() | this.save.isEnabled()) {
			this.generate.setEnabled(false);
			this.save.setEnabled(false);
		}

	}
	
	
	private boolean checkInput() {
		boolean missing = false;
		// check if default value or not
		for (JTextField jtf: this.input_fields) {
			if (jtf.getForeground() == Color.gray) {
				missing = true;
			}
		}
		
		return missing;
	}
	
	private void helpDialog() {
		String html = "<html><body width='350'><p>1. <u>Lebenslauf laden</u>:<br> Bitte einen geeigneten Lebenlauf im PDF- oder JSON - Format laden.<br><br>"
			    + "<p>2. <u>Daten aus Lebenslauf extrahieren und Retrieval durchführen</u>:<br> Die relevanten Attribute des geladenen "
			    + "Lebenslaufs werden extrahiert und angezeigt. Anschließend wird ein CBR-Retrieval mit der angebundenen CaseBase "
			    + "durchgeführt und die ähnlichsten Anschreiben werden zurückgegeben. Liegt die Ähnlichkeit zwischen den Attributen "
			    + "des geladenen Lebenslauf und denen aus der Fallbasis unter 50%, hat der User die Möglichkeit den Vorgang an der Stelle abzubrechen. "
			    + "Es kann ein neuer Lebenslauf geladen werden, um den Vorgang zu wiederholen.<br><br>"
			    + "<p>3. <u>Anschreiben aus abgerufenem Fall generieren</u>:<br> Auf Basis des ähnlichsten Anschreiben welches aus dem CBR-Retrieval "
			    + "hervorging wird mit Hilfe eines ML-Modells ein neues paraphrasiertes Anschreiben generiert und ausgegeben. <br><br></body></html>";
     
		JOptionPane.showMessageDialog(this.getContentPane(),
				html, "Hilfe", JOptionPane.INFORMATION_MESSAGE);
	}
	
	private void saveUserInput() {
		this.userInputMap = new HashMap<String, String>();
		
		for (JTextField jtf: this.input_fields) {
			this.userInputMap.put(jtf.getName(), jtf.getText());
		}
		
	}
	
	private void templateInput() {
		// fill user input fields with examples
		this.nameField.setText("Max Mustermann");
		this.streetField.setText("Musterstraße");
		this.houseNumField.setText("42");
		this.zip_code.setText("66999");
		this.town.setText("Musterstadt");	
		this.salary.setText("55000");
		this.entryDate.setText("01.01.2022");
		
		this.jobNameField.setText("Projektleiter");
		this.jobStreetField.setText("Industrieallee");
		this.jobHouseNumField.setText("666");
		this.job_zip_code.setText("99666");
		this.jobTown.setText("Industriestadt");
		this.companyNameField.setText("Industry AG");
		// set font color to black
		for (JTextField jtf: this.input_fields) {
			jtf.setForeground(Color.BLACK);
		}
	}
	
	private List<Pair<Instance, Similarity>> initiateRetrieval(HashMap<String, ArrayList<String>> query) {
		this.result = null;
		
		// initiate Retrieval with Hash map argument as query
		try {
			CBR_retrieval retrieval = new CBR_retrieval("cv", "cv_cb");
			retrieval.setupCB();
			retrieval.setupAttributes();
			retrieval.createQuery(query);
			
			// save result of query 
			this.result = retrieval.runRetrieval();
			this.logger.info(this.parsedJSON + "\n" + this.result.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// check if similarity over 0.5 and notice user if not so
		if (this.result.get(0).getSecond().getValue() <= 0.5) {
			int option = JOptionPane.showConfirmDialog(this.getContentPane(), 
					"Die Ähnlichkeit zwischen eingegebenem Lebenslauf und dem ähnlichsten, "
					+ "abgerufenem Lebenslauf aus der Fallbasis beträgt weniger als 50 Prozent. ("
					+ this.result.get(0).getSecond().getRoundedValue() + ") "
					+ "\nMöchten Sie das Retrieval trotzdem durchführen?", 
					"Geringe Ähnlichkeit", JOptionPane.YES_NO_OPTION);
			// user accepts retrieval and continues
			if (option == 0) {
				this.textAreaContent.append("\n\n[Coverletter Generator] Retrieval wurde durchgeführt! "
						+ "Bereit zum Generieren von Anschreiben: \"Datei > Anschreiben aus abgerufenem Fall generieren\"");
				this.cvTextArea.setText(this.textAreaContent.toString());
				this.generate.setEnabled(true);
			// user declines or cancels, retrieval is aborted
			} else {
				this.cvTextArea.setText("[Coverletter Generator] Bitte neues Anschreiben auswählen");
				this.retrieve.setEnabled(false);
			}
		// similarity is over 0.5
		} else {
			this.textAreaContent.append("\n\n[Coverletter Generator] Retrieval wurde durchgeführt! "
					+ "Bereit zum Generieren von Anschreiben: \"Datei > Anschreiben aus abgerufenem Fall generieren\"");
			this.cvTextArea.setText(this.textAreaContent.toString());
			this.generate.setEnabled(true);
		}
		System.out.println(this.result);
		return this.result;
	}
	
	private void generateCoverletter() {
		// create filepath 
		final String cbCoverlettersPath= ".\\.\\cb\\coverletters\\";
		String coverletterFileName = this.result.get(0).getFirst().toString();
		String clFilePath = cbCoverlettersPath + coverletterFileName + ".txt";
		
		// Strings for splitting return from paraphraseCoverletter
		String[] returnString = new String[2]; 
		String generatedCoverletter;
		String diversifyMetric;
		// show dialog and ask to use CUDA
		boolean cuda = false;
		int cudaOption = JOptionPane.showConfirmDialog(this.getContentPane(), 
				"Dieser Vorgang kann einige Minuten dauern. Der Einsatz von CUDA verringert die Berrechnungzeit enorm.\n"
				+ "Soll für die Paraphrasierung CUDA verwendet werden?\n\n"
				+ "ACHTUNG: Nur möglich wenn Nvidia GPU vorhanden und CUDA+PyTorch installiert sind, "
				+ "sonst kann die Paraphrasierung nicht durchgeführt werden.",
				"CUDA", JOptionPane.YES_NO_OPTION);
		// cuda yes
		if (cudaOption == 0) {
			this.textAreaContent.append("\n\n[Coverletter Generator] Paraphrasierung mit CUDA gestartet.");
			this.cvTextArea.setText(this.textAreaContent.toString());
			cuda = true;
		// cuda no
		} else {
			this.textAreaContent.append("\n\n[Coverletter Generator] Paraphrasierung ohne CUDA gestartet.");
			this.cvTextArea.setText(this.textAreaContent.toString());
		}
		// generate coverletter
		try {
			this.paraphrasedCLOutput = CL_GeneratorUtils.paraphraseCoverLetter(clFilePath, cuda);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// split up paraphrasedCLPath and assign to strings
		returnString = this.paraphrasedCLOutput.split("[\r\n]+");
		diversifyMetric = returnString[0]; 
		generatedCoverletter = returnString[1]; 
		// ouput of coverletter return
		this.textAreaContent.append("\n\n[Coverletter Generator] Unterschiedlichkeit zum originalen Anschreiben und Dateiname:");
		this.textAreaContent.append("\n[Coverletter Generator] " + diversifyMetric);
		this.textAreaContent.append("\n[Coverletter Generator] " + generatedCoverletter);
		this.cvTextArea.setText(this.textAreaContent.toString());
		// display the actual coverletter content in textarea
		this.displayCoverletter(generatedCoverletter);
		
		System.out.println(returnString[1]);
	}
	
	private void displayCoverletter(String file) {
		// path of project folder containing generated coverletters
		final String CoverlettersPath= ".\\.\\data\\coverletters\\";
		
		try {
			this.textAreaContent.append("\n");
			// concatenate fileName and folder name to path
			Path path = Paths.get(CoverlettersPath + file);
			// read content of generated coverletter txt file
			BufferedReader reader = Files.newBufferedReader(path);
			String line;
			StringBuilder coverletter = new StringBuilder();
			// read file and append to coverletter
			while ((line = reader.readLine()) != null) {
				coverletter.append(line + "\n");
			}
			// assign content of coverletter file to class attribute
			this.paraphrasedCLContent = coverletter.toString();
			// replace date and salary with input from user also replace xy placeholder with ###Eingabe###
			String regexDate = "([0-9]{2}\\.){0,2}([0-9]{4})";
			this.paraphrasedCLContent = this.paraphrasedCLContent.replaceAll("xy", "###Eingabe###");
			this.paraphrasedCLContent = this.paraphrasedCLContent.replaceAll(regexDate, this.entryDate.getText());
			this.paraphrasedCLContent = this.paraphrasedCLContent.replaceAll("xxx", this.salary.getText());
			// display generated coverletter in textarea
			this.textAreaContent.append("\n" + this.paraphrasedCLContent);
			this.cvTextArea.setText(this.textAreaContent.toString());
			this.logger.info(this.paraphrasedCLOutput);
			this.save.setEnabled(true);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void extractCV() throws IOException {
		if (this.cv == null) {
			JOptionPane.showMessageDialog(this.getContentPane(),
				    "Bitte einen Lebenslauf im pdf - oder json - Format laden",
				    "Lebenslauf fehlt",
				    JOptionPane.ERROR_MESSAGE);
		} else if (this.checkInput()){
			JOptionPane.showMessageDialog(this.getContentPane(),
				    "Bitte Eingaben vollständig ausfüllen",
				    "Eingaben fehlen",
				    JOptionPane.ERROR_MESSAGE);
		} else {
			// pdf file
			if (this.cv.getName().endsWith(".pdf")) {
				// parse CV and retrieve file name from utils class
				this.parsedJSON = CL_GeneratorUtils.parse_cv(this.cv.getPath());
				this.textAreaContent.append("\n[Coverletter Generator] PDF Lebenslauf geparsed: " + this.parsedJSON + "\n\n");
				
				// parse CV JSON and receive structured CV data as HashMap
				try {
					this.mappedJSON = CL_GeneratorUtils.JSONtoMap(this.parsedJSON);
				} catch (IOException | ParseException e) {
					e.printStackTrace();
				}
			// json file
			} else if (this.cv.getName().endsWith(".json")) {
				try {
					this.mappedJSON = CL_GeneratorUtils.JSONtoMap(this.cv.getName());
				} catch (IOException | ParseException e) {
					e.printStackTrace();
				}
				this.textAreaContent.append("\n[Coverletter Generator] JSON Lebenslauf geparsed: " + this.parsedJSON + "\n\n");
			}
			
			// show CV data in text area
			this.textAreaContent.append(this.mappedJSON.toString().replace(",", ",\n"));
			this.cvTextArea.setText(this.textAreaContent.toString());
			
			// save input data from user to hashmap
			this.saveUserInput();
			
			// send query to CBR system
			this.initiateRetrieval(this.mappedJSON);
			
		}
	}
	
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				Main m = new Main();
				
				m.setVisible(true);
			}
		});
	}

}

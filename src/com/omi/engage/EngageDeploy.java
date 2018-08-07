package com.omi.engage;

import java.awt.Button;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.omi.bean.DC;
import com.omi.xml.ReadXmlConfig;

public class EngageDeploy {

	private JFrame frame;

	private Choice var_DeployBy;
	private Choice var_DeployType;

	private DefaultListModel model;
	private JList list;

	private JPanel patchGroupPanel;

	private DefaultListModel var_PatchGroup;
	private JList patchGroupList;

	ReadXmlConfig config;

	private JComboBox<String> currentLucasSoftware;
	private JComboBox<String> newLucasSoftware;

	private JCheckBox target;
	private JLabel lbl_targeted;
	private JCheckBox preview;
	private Button btn_ViewLog;
	private Button btnPatchGroup;
	private Button btnServerName;
	private Button btnSiteName;

	private List<DC> sortList;

	private Map<String, Process> processMap;
	private Button btnStatus;

	/**
	 * Launch the application.
	 */

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					EngageDeploy window = new EngageDeploy();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public EngageDeploy() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */

	private void initialize() {

		frame = new JFrame("Lucas Engage Deploy");
		frame.setBackground(SystemColor.window);
		frame.setIconImage(
				Toolkit.getDefaultToolkit().getImage(EngageDeploy.class.getResource("/images/ntt_logo.png")));
		frame.setResizable(false);
		frame.setFont(new Font("Calibri", Font.PLAIN, 14));
		frame.getContentPane().setFont(new Font("Calibri", Font.PLAIN, 14));
		frame.setBounds(100, 100, 650, 629);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setLocationRelativeTo(null);

		// Config Object to call the XML Reading Module
		config = new ReadXmlConfig();
		// configure the process Map

		processMap = new HashMap<String, Process>();

		// setting up the input for Deploy By Param
		JLabel lblDeployBy = new JLabel("Deploy by");
		lblDeployBy.setBounds(16, 8, 123, 23);
		lblDeployBy.setFont(new Font("Calibri", Font.PLAIN, 14));
		frame.getContentPane().add(lblDeployBy);

		var_DeployBy = new Choice();
		lblDeployBy.setLabelFor(var_DeployBy);
		var_DeployBy.setFont(new Font("Calibri", Font.PLAIN, 14));

		var_DeployBy.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				deployByChangedEvent();
				toggleTarget();
			}
		});

		var_DeployBy.setBounds(162, 8, 266, 23);
		var_DeployBy.add("PatchGroup");
		var_DeployBy.add("DCID");

		frame.getContentPane().add(var_DeployBy);

		// Setting up Input for the Deployment Type

		JLabel lblDeploymentType = new JLabel("Deployment Type");
		lblDeploymentType.setBounds(16, 39, 123, 23);
		lblDeploymentType.setFont(new Font("Calibri", Font.PLAIN, 14));
		frame.getContentPane().add(lblDeploymentType);

		var_DeployType = new Choice();
		lblDeploymentType.setLabelFor(var_DeployType);
		var_DeployType.setFont(new Font("Calibri", Font.PLAIN, 14));
		var_DeployType.setBounds(162, 39, 266, 23);

		var_DeployType.add("Full");
		var_DeployType.add("Uninstall Only");
		var_DeployType.add("Install Only");
		var_DeployType.add("3 Step");

		frame.getContentPane().add(var_DeployType);

		// Event Handler on change of Deployment Type to control the Target Checkbox
		// properties
		var_DeployType.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				toggleTarget();
			}
		});

		// Setting inputs for the CUrrent Build of Engage

		JLabel lblCurrentLucasSoftware = new JLabel("Current Engage Build");
		lblCurrentLucasSoftware.setBounds(16, 70, 140, 23);
		lblCurrentLucasSoftware.setFont(new Font("Calibri", Font.PLAIN, 14));
		frame.getContentPane().add(lblCurrentLucasSoftware);

		// currentLucasSoftware = new JFilePicker();
		// currentLucasSoftware.setBounds(162, 70, 262, 30);
		// currentLucasSoftware.addFileTypeFilter("exe", "Executable");
		// currentLucasSoftware.setDefaultDirectory("C:\\Lucas_Tools\\EngageDeployment");

		currentLucasSoftware = new JComboBox<String>();
		lblCurrentLucasSoftware.setLabelFor(currentLucasSoftware);
		currentLucasSoftware.setFont(new Font("Calibri", Font.PLAIN, 14));
		currentLucasSoftware.setBackground(Color.WHITE);
		currentLucasSoftware.setBounds(162, 70, 332, 23);
		frame.getContentPane().add(currentLucasSoftware);

		// Setting inputs for the New Build of Engage

		JLabel lblNewLucasSoftware = new JLabel("New Engage Build");
		lblNewLucasSoftware.setBounds(16, 101, 140, 23);
		lblNewLucasSoftware.setFont(new Font("Calibri", Font.PLAIN, 14));
		frame.getContentPane().add(lblNewLucasSoftware);

		// newLucasSoftware = new JFilePicker();
		// newLucasSoftware.setBounds(162, 97, 262, 30);
		// newLucasSoftware.addFileTypeFilter("exe", "Executable");
		// newLucasSoftware.setDefaultDirectory("C:\\Lucas_Tools\\EngageDeployment");

		newLucasSoftware = new JComboBox<String>();
		lblNewLucasSoftware.setLabelFor(newLucasSoftware);
		newLucasSoftware.setFont(new Font("Calibri", Font.PLAIN, 14));
		newLucasSoftware.setBackground(Color.WHITE);
		newLucasSoftware.setBounds(162, 101, 332, 23);
		frame.getContentPane().add(newLucasSoftware);

		// Populating the SOftware selection Combo Box
		for (String pkg : config.getPackageNames()) {
			currentLucasSoftware.addItem(pkg);
			newLucasSoftware.addItem(pkg);
		}

		// Declaring the Default Selection for the SOftware Combox Box

		// Current Software is the first in the Config File
		currentLucasSoftware.setSelectedIndex(0);

		// New Software is the last software in the config file
		newLucasSoftware.setSelectedIndex(newLucasSoftware.getItemCount() - 1);

		// Setting Up Target Check Box to achieve the Targeted approach
		target = new JCheckBox("Target");

		// Event Handler for the Target Check Box
		// to handle events when selected or unselected
		target.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (target.isSelected()) {

					/*
					 * when selected, the DC must only display the Stand Alone Servers And Only
					 * SIngle selection should be allowed at a time
					 */

					list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					populateDCList(config.getAllPatchGroupID());
				} else {

					/*
					 * When Unselected Multi Select for the DC List must be enabled and The DC List
					 * must be populated as per the selection the selected option of Deploy By
					 */

					lbl_targeted.setText("");
					deployByChangedEvent();
					list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				}
			}
		});
		target.setOpaque(false);
		target.setEnabled(false);

		target.setFont(new Font("Calibri", Font.PLAIN, 14));
		target.setBounds(434, 10, 75, 23);
		frame.getContentPane().add(target);

		lbl_targeted = new JLabel("");
		lbl_targeted.setFont(new Font("Calibri", Font.PLAIN, 14));
		lbl_targeted.setBounds(434, 38, 75, 21);
		frame.getContentPane().add(lbl_targeted);

		// Deploy By Patch Group - Setting Patch Group Selection Panel
		patchGroupPanel = new JPanel();
		// patchGroupPanel.setVisible(false);

		// Label for Patch Group Selection List inside Patch Group Panel
		JLabel lblPatchGroup = new JLabel("Patch Group");
		lblPatchGroup.setBounds(9, 0, 100, 22);
		patchGroupPanel.add(lblPatchGroup);
		lblPatchGroup.setFont(new Font("Calibri", Font.PLAIN, 14));

		patchGroupPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		patchGroupPanel.setBounds(515, 4, 110, 120);

		// Actual Bucket or Model to hold the Patch Group ID
		var_PatchGroup = new DefaultListModel();

		frame.getContentPane().add(patchGroupPanel);
		patchGroupPanel.setLayout(null);

		// Patch Group ID List inside the Patch Group Panel
		patchGroupList = new JList(var_PatchGroup);
		patchGroupList.setLocation(10, 0);
		patchGroupList.setEnabled(false);
		patchGroupList.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {

				// populateDCListByPatchGroup();
				populateDCList(patchGroupList.getSelectedValuesList());

			}
		});

		patchGroupList.setVisibleRowCount(5);

		// Scrollable Bucket for the Patch Group ID List
		JScrollPane patchGroupScrollableList = new JScrollPane();
		patchGroupScrollableList.setBounds(new Rectangle(9, 19, 90, 90));
		patchGroupPanel.add(patchGroupScrollableList);
		// pachGroupPanel.setPreferredSize(new Dimension(400, 200));

		patchGroupScrollableList.setBorder(null);
		patchGroupScrollableList.setViewportView(patchGroupList);

		// Setting up Seperate Panel for the DC List
		JPanel panel = new JPanel();
		panel.setBorder(null);
		panel.setBounds(16, 151, 618, 406);
		frame.getContentPane().add(panel);

		model = new DefaultListModel();
		list = new JList(model);
		list.setSelectionForeground(SystemColor.text);
		list.setSelectionBackground(SystemColor.textHighlight);

		list.setFont(new Font("Monospaced", Font.PLAIN, 12));

		list.setCellRenderer(new DefaultListCellRenderer() {
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {

				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				this.setEnabled(true);
				return this;
			}
		});

		// Action Lister to get the input for the Targeted Approach
		list.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent arg0) {

				/*
				 * This Section is going to pop up a drop box to select the Target Server for
				 * Stand Alone server when the Target Check Box is Selected
				 */

				Thread targerThread = new Thread(new Runnable() {

					@Override
					public void run() {
						if (target.isSelected() && !list.isSelectionEmpty()) {

							String[] choices = list.getSelectedValue().toString().trim().split("\\s+")[2].split(",");
							String targeted = "";
							try {
								targeted = ((String) JOptionPane.showInputDialog(frame, "Select Target Server",
										"TargetCluster Parameter", JOptionPane.OK_CANCEL_OPTION, null, choices,
										choices[0])).trim();
							} catch (Exception e) {
								System.out.println("Target Selection Declined");
							}
							System.out.println(targeted);
							lbl_targeted.setText(targeted);
						}
					}
				});

				targerThread.start();
			}
		});

		// Setting up Scrollable List to fit the DC List -- Inside Panel
		JScrollPane scrollableList = new JScrollPane(list);
		scrollableList.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollableList.setBorder(new LineBorder(new Color(0, 0, 0)));
		scrollableList.setPreferredSize(new Dimension(600, 400));
		panel.add(scrollableList);

		// Setting up Header for DC List -- Inside Panel
		JLabel listHeader = new JLabel("   DC ID   Patch Group     Server Name                   Site Name");
		listHeader.setForeground(Color.BLACK);
		listHeader.setOpaque(true);
		listHeader.setBackground(SystemColor.menu);
		listHeader.setLabelFor(list);
		listHeader.setHorizontalAlignment(SwingConstants.LEFT);
		// scrollableList.setColumnHeaderView(listHeader);
		listHeader.setFont(new Font("Monospaced", Font.PLAIN, 12));

		// Setting up Deploy Button
		Button btn_Deploy = new Button("Deploy");
		btn_Deploy.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btn_Deploy.setName("btn_Deploy");
		btn_Deploy.setFont(new Font("Calibri", Font.BOLD, 14));

		btn_Deploy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// btnDeployClicked();
				btnDeployClickedGrouped();
			}
		});
		btn_Deploy.setBounds(192, 563, 266, 30);
		frame.getContentPane().add(btn_Deploy);

		// Preview the Command for the Deploy - Check Box
		preview = new JCheckBox("Preview");
		preview.setOpaque(false);
		preview.setSelected(true);
		preview.setFont(new Font("Calibri", Font.PLAIN, 14));
		preview.setBounds(467, 563, 75, 30);
		frame.getContentPane().add(preview);

		btn_ViewLog = new Button("Logs");
		btn_ViewLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File file = new File("EngageDeployGUI.log");
				try {
					file.setWritable(false);
					Runtime.getRuntime().exec("notepad " + file.getAbsolutePath());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btn_ViewLog.setName("btn_Deploy");
		btn_ViewLog.setFont(new Font("Calibri", Font.PLAIN, 11));
		btn_ViewLog.setBounds(135, 564, 44, 29);
		frame.getContentPane().add(btn_ViewLog);

		Button btnDCID = new Button("DCID");
		btnDCID.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				printList("sortDCID");
			}
		});
		btnDCID.setName("btn_Deploy");
		btnDCID.setFont(new Font("Calibri", Font.PLAIN, 11));
		btnDCID.setBounds(26, 135, 65, 21);
		frame.getContentPane().add(btnDCID);

		btnPatchGroup = new Button("Patch Group");
		btnPatchGroup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				printList("sortPatchGroupId");
			}
		});
		btnPatchGroup.setName("btn_Deploy");
		btnPatchGroup.setFont(new Font("Calibri", Font.PLAIN, 11));
		btnPatchGroup.setBounds(95, 135, 99, 21);
		frame.getContentPane().add(btnPatchGroup);

		btnServerName = new Button("Server Name");
		btnServerName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				printList("sortServerName");
			}
		});
		btnServerName.setName("btn_Deploy");
		btnServerName.setFont(new Font("Calibri", Font.PLAIN, 11));
		btnServerName.setBounds(200, 135, 215, 21);
		frame.getContentPane().add(btnServerName);

		btnSiteName = new Button("Site Name");
		btnSiteName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				printList("sortSiteName");
			}
		});
		btnSiteName.setName("btn_Deploy");
		btnSiteName.setFont(new Font("Calibri", Font.PLAIN, 11));
		btnSiteName.setBounds(422, 135, 199, 21);
		frame.getContentPane().add(btnSiteName);

		btnStatus = new Button("Status");

		btnStatus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File folder = new File("C:\\Lucas_Tools\\EngageDeployment\\Status\\");
				File[] listOfFiles = folder.listFiles();

				String files = "";

				for (int i = 0; i < listOfFiles.length; i++) {
					if (listOfFiles[i].isFile()) {
						if (!files.equalsIgnoreCase("")) {
							files += "\n\r";
						}
						files += listOfFiles[i].getName();
						System.out.println("File " + listOfFiles[i].getName());
					}
				}

				if (files.length() > 0) {
					try {

						int listJOptionReturn = JOptionPane.NO_OPTION;

						listJOptionReturn = JOptionPane.showConfirmDialog(frame,
								"Process Running on Servers : \n\r" + files + " \n\r\n\r Remove Status File? ",
								"Status", JOptionPane.YES_NO_OPTION);
						if (listJOptionReturn == JOptionPane.YES_OPTION) {
							File deleteStatusFile = ((File) JOptionPane.showInputDialog(frame, "Remove Status File? ",
									"Remove Status File", JOptionPane.OK_CANCEL_OPTION, null, listOfFiles, null));

							deleteStatusFile.delete();

						}
					} catch (Exception e1) {
						e1.printStackTrace();
						System.out.println("Status File Not Removed");
					}
				} else {

					JOptionPane.showMessageDialog(frame, "No Deployment Processes Running");

				}
			}
		});

		btnStatus.setName("btn_Deploy");
		btnStatus.setFont(new Font("Calibri", Font.PLAIN, 11));
		btnStatus.setBounds(72, 563, 44, 29);
		frame.getContentPane().add(btnStatus);

		Button btnConfig = new Button("Config");
		btnConfig.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					String app = ((String) JOptionPane.showInputDialog(frame, "Open With", "Edit Configuration File",
							JOptionPane.OK_CANCEL_OPTION, null, "notepad,notepad++".split(","), null)).trim();

					Runtime.getRuntime().exec("cmd /c start " + app + " engage_deployment_config.xml ");

					// Runtime.getRuntime().exec("start " + app + );
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					System.out.println(e1.getMessage());
					;
				}

			}
		});
		btnConfig.setName("btn_Deploy");
		btnConfig.setFont(new Font("Calibri", Font.PLAIN, 11));
		btnConfig.setBounds(548, 563, 44, 29);
		frame.getContentPane().add(btnConfig);

		// Initialize the list with items
		// populateDCList(config.getAllPatchGroupID());
		deployByChangedEvent();
	}

	private void populateDCList(List<String> selectedPatchGroup) {

		model.removeAllElements();

		Collections.sort(selectedPatchGroup);

		int target = var_DeployType.getSelectedItem().equalsIgnoreCase("3 Step") ? 2 : 0;
		if (this.target.isSelected())
			target = 1;

		List<DC> DCids = config.method(selectedPatchGroup, target);

		// taking backup of the List
		sortList = DCids;

		if (DCids.size() > 0) {

			if (!var_DeployBy.getSelectedItem().equalsIgnoreCase(("PatchGroup")))
				Collections.sort(DCids);

			for (int i = 0; i < DCids.size(); i++) {
				// System.out.println(DCids.get(i));
				model.add(i, DCids.get(i));
			}
		}
	}

	private void deployByChangedEvent() {

		if (var_DeployBy.getSelectedItem().equalsIgnoreCase("PATCHGROUP")) {
			var_PatchGroup.removeAllElements();
			patchGroupList.setEnabled(true);

			// Clear the DC List
			model.removeAllElements();

			// Populate the Patch Group List

			List<String> patchGroupIdsList = config.getAllPatchGroupID();
			Collections.sort(patchGroupIdsList);
			var_PatchGroup.removeAllElements();
			for (int i = 0; i < patchGroupIdsList.size(); i++) {
				var_PatchGroup.add(i, patchGroupIdsList.get(i));
			}

			list.setEnabled(false);

		} else {
			list.setEnabled(true);

			patchGroupList.setEnabled(false);
			var_PatchGroup.removeAllElements();
			populateDCList(config.getAllPatchGroupID());

		}

	}

	private void printList(String sort) {

		// clear the Model/List
		model.removeAllElements();

		Collections.sort(sortList);

		if (sort.equals("sortPatchGroupId")) {
			Collections.sort(sortList, new sortPatchGroupId());
		} else if (sort.equals("sortSiteName")) {
			Collections.sort(sortList, new sortSiteName());
		} else if (sort.equals("sortServerName")) {
			Collections.sort(sortList, new sortServerName());
		}

		for (int i = 0; i < sortList.size(); i++) {
			// System.out.println(DCids.get(i));
			model.add(i, sortList.get(i));
		}
	}

	private void toggleTarget() {

		if ((var_DeployType.getSelectedItem().equals("Uninstall Only")
				|| var_DeployType.getSelectedItem().equals("Install Only"))
				&& var_DeployBy.getSelectedItem().equals("DCID")) {
			target.setEnabled(true);
		} else {
			target.setEnabled(false);
			target.setSelected(false);
			lbl_targeted.setText("");
		}

		if (var_DeployBy.getSelectedItem().equals("PatchGroup"))
			populateDCList(patchGroupList.getSelectedValuesList());
		else
			populateDCList(config.getAllPatchGroupID());

	}

	private void btnDeployClickedGrouped() {

		String prefix = "C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe -ExecutionPolicy ByPass -File \"C:\\Lucas_Tools\\EngageDeployment\\engage_deployment.ps1\" ";
		String command = prefix;

		// Find the Number of unique servers selected from the list
		Set<String> ServerList = new TreeSet<String>();

		if (var_DeployBy.getSelectedItem().equals("DCID")) {
			if (list.getSelectedValuesList().size() == 0) {
				JOptionPane.showMessageDialog(frame, "DC Not Selected!");
				return;
			}
			for (Object itr_servers : list.getSelectedValuesList()) {
				ServerList.add(itr_servers.toString().split(("\\s+"))[3]);
			}

		} else {
			if (patchGroupList.getSelectedValuesList().size() == 0) {
				JOptionPane.showMessageDialog(frame, "Patch Group Not Selected!");
				return;
			}
			ListModel myPatchGroupModel = list.getModel();

			for (int i = 0; i < myPatchGroupModel.getSize(); i++) {
				ServerList.add(myPatchGroupModel.getElementAt(i).toString().split(("\\s+"))[3]);
			}
		}

		// System.out.println(ServerList);

		// create command per server and trigger individual cmd.exe for each connection

		for (String currentServer : ServerList) {

			command += "-byDCID -DCID ";
			String DCID = "";

			if (var_DeployBy.getSelectedItem().equals("DCID")) {

				for (Object itr_dcid : list.getSelectedValuesList()) {
					String selectedServer = itr_dcid.toString().split("\\s+")[3];
					// System.out.println("Current Server : " + currentServer + " Selected Server :
					// " + selectedServer);
					if (selectedServer.equalsIgnoreCase(currentServer)) {
						if (!DCID.equals("")) {
							DCID += ",";
						}
						String selectedList = (itr_dcid.toString().trim().split("\\s+"))[0];
						DCID += selectedList;
					}
				}
				// System.out.println(DCID);
				command += DCID;
			} else {

				if (patchGroupList.getSelectedValuesList().size() == 0) {
					JOptionPane.showMessageDialog(frame, "No Patch Group have been selected");
					return;
				}

				ListModel myPatchGroupModel = list.getModel();

				for (int i = 0; i < myPatchGroupModel.getSize(); i++) {
					String selectedServer = myPatchGroupModel.getElementAt(i).toString().split(("\\s+"))[3];
					// System.out.println("Current Server : " + currentServer + " Selected Server :
					// " + selectedServer);
					if (selectedServer.equalsIgnoreCase(currentServer)) {
						if (!DCID.equals("")) {
							DCID += ",";
						}

						// System.out.println(myPatchGroupModel.getElementAt(i));

						String selectedList = myPatchGroupModel.getElementAt(i).toString().trim().split("\\s+")[0];
						DCID += selectedList;
					}

				}
				// System.out.println(DCID);
				command += DCID;
			}

			String deploymentType = var_DeployType.getSelectedItem();
			// System.out.println("Deployment Type = " + deploymentType);

			if (deploymentType.equals("Full")) {
				command += " -deploymentType \"F\"";

				if (((String) currentLucasSoftware.getSelectedItem()).equalsIgnoreCase("")
						|| ((String) newLucasSoftware.getSelectedItem()).equalsIgnoreCase("")) {
					JOptionPane.showMessageDialog(frame, "Please Enter the Software Names");
					return;
				}

			} else if (deploymentType.equals("Uninstall Only")) {
				command += " -deploymentType \"U\"";

				if (currentLucasSoftware.getSelectedItem().equals("")) {
					JOptionPane.showMessageDialog(frame, "Please Enter Current Software Names");
					return;
				}
			} else if (deploymentType.equals("Install Only")) {
				command += " -deploymentType \"I\"";

				if (currentLucasSoftware.getSelectedItem().equals("")) {
					JOptionPane.showMessageDialog(frame, "Please Enter Current Software Names");
					return;
				}
			} else if (deploymentType.equals("3 Step")) {
				command += " -3Step";
				command += " -deploymentType \"F\"";
				if (currentLucasSoftware.getSelectedItem().equals("")
						|| newLucasSoftware.getSelectedItem().equals("")) {
					JOptionPane.showMessageDialog(frame, "Please Enter the Software Names");
					return;
				}
			}

			if (target.isSelected()) {
				if (lbl_targeted.getText().length() > 0)
					command += " -clusterTarget " + lbl_targeted.getText();
				else {
					JOptionPane.showMessageDialog(frame, "Target Server Not Selected!");
					return;
				}
			}

			command += " -currentLucasSoftware " + currentLucasSoftware.getSelectedItem() + " -newLucasSoftware "
					+ newLucasSoftware.getSelectedItem();

			System.out.println(command);
			int dialogResult = 0;

			if (preview.isSelected()) {
				dialogResult = JOptionPane.showConfirmDialog(frame,
						"<html><body><p style='width: 500px;'> Deployment Process will be executed on : "
								+ (target.isSelected() ? lbl_targeted.getText() : currentServer) + "<br><br>" + command
								+ "<br><br><center>Execute?</center></p></body></html>",
						"Command Preview", JOptionPane.YES_NO_OPTION);
			}

			if (dialogResult == 0) {

				BufferedWriter bw = null;
				FileWriter fw = null;

				try {

					// create a flag File to track the process
					String status = "";
					for (String server : currentServer.split(",")) {
						status += server;
					}

					File statusFile = new File("C:\\Lucas_Tools\\EngageDeployment\\Status\\" + status);

					if (statusFile.exists()) {
						JOptionPane.showMessageDialog(frame,
								"Deployment Process running on server "
										+ (target.isSelected() ? lbl_targeted.getText() : currentServer)
										+ "\n\rDeployment Request Terminated!");
					} else {

						statusFile.createNewFile();

						System.out.println("File Created : " + statusFile.getAbsolutePath());
						command += " -status " + status;

						System.out.println(command);

						File file = new File("EngageDeployGUI.log");

						// if file doesnt exists, then create it
						if (!file.exists()) {
							file.createNewFile();
						}

						// true = append file
						file.setWritable(true);

						fw = new FileWriter(file.getAbsoluteFile(), true);
						bw = new BufferedWriter(fw);

						// Run the Command on the CMD Prompt
						Runtime.getRuntime().exec("cmd /c start cmd.exe /K \"" + command + "\"");

						bw.write(new Date() + " : " + (target.isSelected() ? lbl_targeted.getText() : currentServer)
								+ " : Command >> " + command + "\r\n");

						file.setWritable(false);
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {

					try {
						if (bw != null)
							bw.close();

						if (fw != null)
							fw.close();

					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}

			} else {
				System.out.println("User Rejected to execute");
			}
			command = prefix;

			// "C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe
			// -ExecutionPolicy ByPass -File
			// \"C:\\Lucas_Tools\\EngageDeployment\\engage_deployment.ps1\" ";

		}
		// list.setSelectedIndex(-1);
		deployByChangedEvent();
	}
}

// Comparators for The DC List
class sortDCID implements Comparator<DC> {
	public int compare(DC a, DC b) {
		return a.getDcid() - b.getDcid();
	}
}

class sortPatchGroupId implements Comparator<DC> {

	public int compare(DC a, DC b) {
		return a.getPatchGroupId().compareTo(b.getPatchGroupId());
	}
}

class sortSiteName implements Comparator<DC> {

	public int compare(DC a, DC b) {
		return a.getSiteName().compareTo(b.getSiteName());
	}
}

class sortServerName implements Comparator<DC> {

	public int compare(DC a, DC b) {
		return a.getServerName().compareTo(b.getServerName());
	}
}

package hk.zdl.crypto.pearlet.component.settings.wizard;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import hk.zdl.crypto.pearlet.util.Util;
import se.gustavkarlsson.gwiz.AbstractWizardPage;
import se.gustavkarlsson.gwiz.Wizard;
import se.gustavkarlsson.gwiz.WizardController;

public class JDialogWizard extends JDialog implements Wizard {

	private static final long serialVersionUID = -4056365152045590112L;
	private static final ResourceBundle rsc_bdl = Util.getResourceBundle();
	private static final Dimension defaultminimumSize = new Dimension(640, 480);
	private final JPanel wizardPageContainer = new JPanel(new GridLayout(1, 1));
	private final JButton backButton = new JButton(rsc_bdl.getString("SETTINGS.NETWORK.WIZARD.DIALOG.BACK"));
	private final JButton nextButton = new JButton(rsc_bdl.getString("SETTINGS.NETWORK.WIZARD.DIALOG.NEXT"));
	private final JButton finishButton = new JButton(rsc_bdl.getString("SETTINGS.NETWORK.WIZARD.DIALOG.FINISH"));
	private final JButton cancelButton = new JButton(UIManager.getString("OptionPane.cancelButton.textAndMnemonic"));
	private final CardLayout card = new CardLayout();
	private final JPanel c = new JPanel(card);
	private boolean finish = false;

	public JDialogWizard() {
		setupWizard();
	}

	public JDialogWizard(Frame owner) {
		super(owner);
		setupWizard();
	}

	public JDialogWizard(Window owner) {
		super(owner);
		setupWizard();
	}

	public JDialogWizard(Frame owner, String title) {
		super(owner, title);
		setupWizard();
	}

	public JDialogWizard(Window owner, String title) {
		super(owner, title);
		setupWizard();
	}

	private void setupWizard() {
		setupComponents();
		layoutComponents();

		setMinimumSize(defaultminimumSize);
		setPreferredSize(defaultminimumSize);
		setResizable(false);
		pack();
		setLocationRelativeTo(null);

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
		getRootPane().registerKeyboardAction(e -> {
			if (nextButton.isEnabled()) {
				nextButton.doClick();
			} else if (finishButton.isEnabled()) {
				finishButton.doClick();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

	private void setupComponents() {
		c.add(nextButton, "n");
		c.add(finishButton, "f");
		cancelButton.addActionListener((e) -> dispose());

		finishButton.addActionListener((e) -> {
			finish = true;
			dispose();
		});

		wizardPageContainer.addContainerListener(new MinimumSizeAdjuster());

		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
	}

	private void layoutComponents() {
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(wizardPageContainer, BorderLayout.CENTER);
		var btn_panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		Stream.of(backButton, c, cancelButton).forEach(btn_panel::add);
		getContentPane().add(btn_panel, BorderLayout.SOUTH);
	}

	@Override
	public Container getWizardPageContainer() {
		return wizardPageContainer;
	}

	@Override
	public AbstractButton getCancelButton() {
		return cancelButton;
	}

	@Override
	public AbstractButton getPreviousButton() {
		return backButton;
	}

	@Override
	public AbstractButton getNextButton() {
		return nextButton;
	}

	@Override
	public AbstractButton getFinishButton() {
		SwingUtilities.invokeLater(() -> card.show(c, finishButton.isEnabled() ? "f" : "n"));
		return finishButton;
	}

	public boolean isFinish() {
		return finish;
	}

	private class MinimumSizeAdjuster implements ContainerListener {

		@Override
		public void componentAdded(ContainerEvent e) {
			Dimension currentSize = getSize();
			Dimension preferredSize = getPreferredSize();

			Dimension newSize = new Dimension(currentSize);
			newSize.width = Math.max(currentSize.width, preferredSize.width);
			newSize.height = Math.max(currentSize.height, preferredSize.height);

			setMinimumSize(newSize);
		}

		@Override
		public void componentRemoved(ContainerEvent e) {
		}

	}

	public static final boolean showWizard(String title, AbstractWizardPage startPage, Container... c) {
		var w = c.length > 0 ? SwingUtilities.getWindowAncestor(c[0]) : null;
		var wizard = new JDialogWizard(w, title);
		var controller = new WizardController(wizard);
		controller.startWizard(startPage);
		wizard.setVisible(true);
		return wizard.isFinish();
	}
}

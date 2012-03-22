/* MainWindow.java

{{IS_NOTE
	Purpose:

	Description:

	History:
		Dec 20, 2007 12:40:46 PM     2007, Created by Dennis.Chenf
		Jan 1, 2009 modified by kinda lu
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

 */
package org.zkoss.zss.app;

import java.util.Iterator;

import org.zkoss.lang.Library;
import org.zkoss.poi.ss.usermodel.Cell;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.ForwardEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zss.app.ctrl.HeaderSizeCtrl;
import org.zkoss.zss.app.ctrl.RenameSheetCtrl;
import org.zkoss.zss.app.file.FileHelper;
import org.zkoss.zss.app.formula.FormulaMetaInfo;
import org.zkoss.zss.app.zul.Dialog;
import org.zkoss.zss.app.zul.FileMenu;
import org.zkoss.zss.app.zul.ViewMenu;
import org.zkoss.zss.app.zul.Zssapp;
import org.zkoss.zss.app.zul.Zssapps;
import org.zkoss.zss.app.zul.ctrl.DesktopCellStyleContext;
import org.zkoss.zss.app.zul.ctrl.DesktopWorkbenchContext;
import org.zkoss.zss.app.zul.ctrl.SSRectCellStyle;
import org.zkoss.zss.app.zul.ctrl.SSWorkbookCtrl;
import org.zkoss.zss.app.zul.ctrl.WorkbenchCtrl;
import org.zkoss.zss.app.zul.ctrl.WorkbookCtrl;
import org.zkoss.zss.engine.event.SSDataEvent;
import org.zkoss.zss.model.Range;
import org.zkoss.zss.model.Ranges;
import org.zkoss.zss.model.Worksheet;
import org.zkoss.zss.model.impl.BookHelper;
import org.zkoss.zss.ui.Action;
import org.zkoss.zss.ui.Position;
import org.zkoss.zss.ui.Rect;
import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zss.ui.event.CellSelectionEvent;
import org.zkoss.zss.ui.event.KeyEvent;
import org.zkoss.zss.ui.impl.MergeMatrixHelper;
import org.zkoss.zss.ui.impl.MergedRect;
import org.zkoss.zss.ui.impl.Utils;
import org.zkoss.zss.ui.sys.ActionHandler;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Menu;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

/**
 * @author Peter Kuo
 * @modify kinda lu
 */
public class MainWindowCtrl extends GenericForwardComposer implements WorkbenchCtrl {

	private static final long serialVersionUID = 1;
	
	private final static String KEY_PDF = "org.zkoss.zss.app.exportToPdf";
	private final static String KEY_HTML = "org.zkoss.zss.app.exportToHtml";
	
	static int event_x = 200;
	static int event_y = 200;

	int lastRow = 0;
	int lastCol = 0;
	boolean isFreezeRow = false;
	boolean isFreezeColumn = false;
	
	Spreadsheet spreadsheet;
	
	// For fast Icon
	boolean isMergeCell = false;

	int chartKey = 0;

	RangeHelper rangeh;

	//Window mainWin;
	Div mainWin;
	/*Menus*/
	FileMenu fileMenu;
	ViewMenu viewMenu;
	
//	ColumnHeaderMenupopup columnHeaderMenupopup;
//	RowHeaderMenupopup rowHeaderMenupopup;
	
	/*dropdown button menu*/
//	Menuitem filter;
//	Menuitem clearFilter;
//	Menuitem reapplyFilter;
	
	Menu insertImageMenu;
	Menu insertPieChart;
	
	/* Toolbar buttons */
	Borderlayout topToolbars;
//	Div toolbarMask;
	
//	Dropdownbutton pasteDropdownBtn;
//	Dropdownbutton sortDropdownBtn;
//	Toolbarbutton saveBtn;
//	Toolbarbutton closeBtn;
//	CellStyleCtrlPanel fontCtrlPanel;
//	Toolbarbutton exportToPDFBtn;
//	Toolbarbutton insertHyperlinkBtn;
//	Toolbarbutton mergeCellBtn;
//	Toolbarbutton insertChartBtn;
//	Toolbarbutton cutBtn;
//	Toolbarbutton copyBtn;
//	Toolbarbutton insertImageBtn;
//	Checkbox gridlinesCheckbox;
//	Checkbox protectSheet;
	
//	CellContext cellContext;
//	CellMenupopup cellMenupopup;
	
	/*Dialog*/
	Dialog _insertFormulaDialog;
	Dialog _insertHyperlinkDialog;
	Dialog _pasteSpecialDialog;
	Dialog _composeFormulaDialog;
	Dialog _formatNumberDialog;
	Dialog _saveFileDialog;
	Dialog _headerSizeDialog;
	Dialog _renameSheetDialog;
	Dialog _openFileDialog;
	Dialog _importFileDialog;
	Dialog _customSortDialog;
	Dialog _exportToPdfDialog;
	Dialog _exportToHtmlDialog;
	
	MainActionHandler actionHandler;

	public Spreadsheet getSpreadsheet() {
		return spreadsheet;
	}

	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		
		spreadsheet.setActionHandler(actionHandler = new MainActionHandler());
		
		//TODO: do it after "afterCompose"
		FileHelper.openNewSpreadsheet(spreadsheet);
		
		//TODO: replace this mechanism
		initZssappComponents();
		init();
		rangeh = new RangeHelper(spreadsheet);
	}
	
	public void onCreate() {
		getDesktopWorkbenchContext().fireWorkbookChanged();
	}
	
	//TODO: remove this mechanism
	private void initZssappComponents() {
		Zssapps.bindSpreadsheet(spreadsheet, this);
	}
	
	public void init() {
		boolean isPE = WebApps.getFeature("pe");
//		exportToPDFBtn.setDisabled(!isPE);
//		filter.setDisabled(!isPE);
		
		//Note. setSrcName will set spreadsheet's src name, but not the book name
		// if setSrc will init a book, then setSrcName only change the src name, 
		// if setSrc again with the first same book, the book will register two listener 
		//spreadsheet.setSrcName("Untitled");
		final DesktopWorkbenchContext workbenchContext = getDesktopWorkbenchContext();
		workbenchContext.doTargetChange(new SSWorkbookCtrl(spreadsheet));
		workbenchContext.setWorkbenchCtrl(this);
		
//		if (!FileHelper.hasSavePermission())
//			saveBtn.setVisible(false);
		workbenchContext.addEventListener(Consts.ON_WORKBOOK_SAVED,	new EventListener() {
			public void onEvent(Event event) throws Exception {
				if (!FileHelper.hasSavePermission())
					return;
				
				spreadsheet.setActionDisabled(true, Action.SAVE_BOOK);
			}
		});

		workbenchContext.addEventListener(Consts.ON_WORKBOOK_CHANGED, new EventListener() {
			public void onEvent(Event event) throws Exception {
				boolean isOpen = spreadsheet.getBook() != null;
//				toolbarMask.setVisible(!isOpen);
//				closeBtn.setVisible(isOpen);
				
				spreadsheet.setActionDisabled(true, Action.SAVE_BOOK);

//				gridlinesCheckbox.setChecked(isOpen && spreadsheet.getSelectedSheet().isDisplayGridlines());
//				protectSheet.setChecked(isOpen && spreadsheet.getSelectedSheet().getProtect());
//				protectSheet.setDisabled(!isOpen);
				
				//TODO: provide clip board interface, to allow save cut, copy, high light info
				//use set setHighlight null can cancel selection, but need to re-store selection when select same sheet again
				spreadsheet.setHighlight(null);
				
				if (isOpen) {
					getCellStyleContext().doTargetChange(
							new SSRectCellStyle(Utils.getOrCreateCell(spreadsheet.getSelectedSheet(), 0, 0), spreadsheet));
//					syncAutoFilterStatus();
				}
			}
		});
		workbenchContext.addEventListener(Consts.ON_SHEET_CHANGED, new EventListener() {
			public void onEvent(Event event) throws Exception {
//				gridlinesCheckbox.setChecked(spreadsheet.getSelectedSheet().isDisplayGridlines());
//				protectSheet.setChecked(spreadsheet.getSelectedSheet().getProtect());
//				syncAutoFilterStatus();
			}
		});
		workbenchContext.addEventListener(Consts.ON_SHEET_CONTENTS_CHANGED,  new EventListener(){
			public void onEvent(Event event) throws Exception {
				doContentChanged();
			}}
		);
		//TODO: remove to WorkbookCtrl
//		workbenchContext.addEventListener(Consts.ON_SHEET_MERGE_CELL, new EventListener() {
//			public void onEvent(Event event) throws Exception {
//				onMergeCellClick(null);
//			}
//		});
		
		workbenchContext.addEventListener(Consts.ON_SHEET_INSERT_FORMULA, new EventListener() {
			public void onEvent(Event event) throws Exception {
				String formula = (String)event.getData();
				Rect rect = spreadsheet.getSelection();
				Range rng = Ranges.range(spreadsheet.getSelectedSheet(), rect.getTop(), rect.getLeft());
				rng.setEditText(formula);
			}
		});
		workbenchContext.getWorkbookCtrl().addBookEventListener(new EventListener() {
			public void onEvent(Event event) throws Exception {
				String evtName = event.getName();
				if (evtName == SSDataEvent.ON_CONTENTS_CHANGE) {
					doContentChanged();
				}
			}
		});
		
		//TODO: rm ON_CELL_FOUCSED listener
//		spreadsheet.addEventListener(Events.ON_CELL_FOUCSED,
//				new EventListener() {
//					public void onEvent(Event event) throws Exception {
//						doFocusedEvent((CellEvent) event);
//					}
//				});

//		spreadsheet.addEventListener(Events.ON_CELL_RIGHT_CLICK,
//				new EventListener() {
//					public void onEvent(Event event) throws Exception {
//						System.out.println("ON_CELL_RIGHT_CLICK: " + event);
////						doMouseEvent((CellMouseEvent) event);
//					}
//				});

//		spreadsheet.addEventListener(Events.ON_HEADER_RIGHT_CLICK,
//				new EventListener() {
//					public void onEvent(Event event) throws Exception {
//						doHeaderMouseEvent((HeaderMouseEvent) event);
//					}
//				});

		//TODO: rm ON_CELL_SELECTION listener
//		spreadsheet.addEventListener(Events.ON_CELL_SELECTION,
//				new EventListener() {
//					public void onEvent(Event event) throws Exception {
//						doSelectionEvent((CellSelectionEvent) event);
//					}
//				});
		
		//TODO: rm ON_CELL_RIGHT_CLICK listener
//		spreadsheet.addEventListener(Events.ON_CELL_RIGHT_CLICK, 
//				new EventListener() {
//					public void onEvent(Event event) throws Exception {
//						CellMouseEvent evt = (CellMouseEvent)event;
//						int clientX = evt.getClientx();
//						int clientY = evt.getClienty();
//						cellContext.setLeft(Integer.toString(clientX + 5) + "px");
//						cellContext.setTop(Integer.toString(clientY - 100) + "px");
//						cellContext.doPopup();
//					}
//				});
		
//		spreadsheet.addEventListener(Events.ON_START_EDITING, 
//				new EventListener() {
//					public void onEvent(Event event) throws Exception {
//						EditHelper.clearCutOrCopy(spreadsheet);
//					}
//				});
		
	}

	public void doContentChanged() {
		//enable SAVE_BOOK button
		spreadsheet.setActionDisabled(false, Action.SAVE_BOOK);
		
		Worksheet seldSheet = spreadsheet.getSelectedSheet();
		Rect seld =  spreadsheet.getSelection();
		int row = seld.getTop();
		int col = seld.getLeft();
		Cell cell = Utils.getCell(seldSheet, row, col);
		if (cell != null) {
			getCellStyleContext().doTargetChange(new SSRectCellStyle(cell, spreadsheet));
		}
	}
	
	public void saveBook() {
		DesktopWorkbenchContext workbench = getDesktopWorkbenchContext();
		if (workbench.getWorkbookCtrl().hasFileExtentionName()) {
			workbench.getWorkbookCtrl().save();
			workbench.fireWorkbookSaved();
		} else
			workbench.getWorkbenchCtrl().openSaveFileDialog();
	}

//	public void syncAutoFilterStatus() {
//		final Worksheet worksheet = spreadsheet.getSelectedSheet();
//		boolean appliedFilter = false;
//		AutoFilter af = worksheet.getAutoFilter();
//		if (af != null) {
//			final CellRangeAddress afrng = af.getRangeAddress();
//			if (afrng != null) {
//				int rowIdx = afrng.getFirstRow() + 1;
//				for (int i = rowIdx; i <= afrng.getLastRow(); i++) {
//					final Row row = worksheet.getRow(i);
//					if (row != null && row.getZeroHeight()) {
//						appliedFilter = true;
//						break;
//					}
//				}	
//			}
//		}
////		clearFilter.setDisabled(!appliedFilter);
////		reapplyFilter.setDisabled(!appliedFilter);
//	}
	
	private WorkbookCtrl getWorkbookCtrl() {
		return  getDesktopWorkbenchContext().getWorkbookCtrl();
	}
	
	private DesktopWorkbenchContext getDesktopWorkbenchContext() {
		return Zssapp.getDesktopWorkbenchContext(self);
	}
	
//	void doFocusedEvent(final CellEvent event) {
//		final Book book = spreadsheet.getBook();
//		if (book == null) {
//			return;
//		}
//		// SECTION WORK1 FocusedEvent
//		try {
//			Worksheet sheet = event.getSheet();
//			lastRow = event.getRow();
//			lastCol = event.getColumn();
//
//			Cell cell = Utils.getOrCreateCell(sheet, lastRow, lastCol);
//			// read format from cell and assign it to toolbar
//			// merge cell
//			isMergeCell = isMergedCell(event.getRow(), event.getColumn(), event.getRow(), event.getColumn());
////			mergeCellBtn.setSclass(isMergeCell ? "clicked" : null);
//
//			getCellStyleContext().doTargetChange(new SSRectCellStyle(cell, spreadsheet));
//
//		} catch (Exception e) {
//		}
//	}
	
	private boolean isMergedCell(int tRow, int lCol, int bRow, int rCol) {
		MergeMatrixHelper mmhelper = spreadsheet.getMergeMatrixHelper(spreadsheet.getSelectedSheet());
		for (final Iterator iter = mmhelper.getRanges().iterator(); iter
				.hasNext();) {
			MergedRect block = (MergedRect) iter.next();
			int bt = block.getTop();
			int bl = block.getLeft();
			int bb = block.getBottom();
			int br = block.getRight();
			if (lCol <= bl && tRow <= bt && rCol >= br && bRow >= bb) {
				return true;
			}
		}
		return false;
	}

	public void doSelectionEvent(CellSelectionEvent event) {
		isMergeCell = isMergedCell(
				spreadsheet.getSelection().getTop(), 
				spreadsheet.getSelection().getLeft(), 
				spreadsheet.getSelection().getBottom(),
				spreadsheet.getSelection().getRight());

//		mergeCellBtn.setSclass(isMergeCell ? "clicked" : null);
	}
	
	public void onCtrlKey$spreadsheet(KeyEvent event) {
		Rect selection = event.getSelection();
		char c = (char) event.getKeyCode();
		try {
			if (46 == event.getKeyCode()) {// delete
				if (event.isCtrlKey())
					actionHandler.doClearStyle(selection);
				else
					actionHandler.doClearContent(selection);
				return;
			}

			if (false == event.isCtrlKey())
				return;

			switch (c) {
			case 'X':
				actionHandler.doCut(selection);
				break;
			case 'C':
				actionHandler.doCopy(selection);
				break;
			case 'V':
				actionHandler.doPaste(selection);
				break;
			case 'D':
				actionHandler.doClearContent(selection);
				break;
			case 'E':// for testing
				spreadsheet.setCellFocus(new Position(2, 2));
				spreadsheet.setSelection(new Rect(2, 2, 4, 4));
				break;
			case 'S':
				//TODO: check permission from WorkbookCtrl
				if (FileHelper.hasSavePermission()) {
					//TODO: refactor duplicate save logic
					DesktopWorkbenchContext workbench = getDesktopWorkbenchContext();
					if (workbench.getWorkbookCtrl().hasFileExtentionName()) {
						workbench.getWorkbookCtrl().save();
						workbench.fireWorkbookSaved();
					} else
						workbench.getWorkbenchCtrl().openSaveFileDialog();
				}
				break;
			case 'O':
				openOpenFileDialog();
				break;
			case 'B':
				actionHandler.doFontBold(selection);
//				getCellStyleContext().modifyStyle(new StyleModification(){
//					public void modify(org.zkoss.zss.app.zul.ctrl.CellStyle style, CellStyleContextEvent candidteEvt) {
//						candidteEvt.setExecutor(MainWindowCtrl.this);
//						style.setBold(!style.isBold());
//					}
//				});
				break;
			case 'I':
				actionHandler.doFontItalic(selection);
//				getCellStyleContext().modifyStyle(new StyleModification(){
//					public void modify(org.zkoss.zss.app.zul.ctrl.CellStyle style, CellStyleContextEvent candidteEvt) {
//						candidteEvt.setExecutor(MainWindowCtrl.this);
//						style.setItalic(!style.isItalic());
//					}
//				});
				break;
			case 'U':
				actionHandler.doFontUnderline(selection);
//				getCellStyleContext().modifyStyle(new StyleModification(){
//					public void modify(org.zkoss.zss.app.zul.ctrl.CellStyle style, CellStyleContextEvent candidteEvt) {
//						candidteEvt.setExecutor(MainWindowCtrl.this);
//						boolean isUnderline = style.getUnderline() ==  org.zkoss.zss.app.zul.ctrl.CellStyle.UNDERLINE_SINGLE;
//						style.setUnderline(isUnderline ? 
//								org.zkoss.zss.app.zul.ctrl.CellStyle.UNDERLINE_NONE : org.zkoss.zss.app.zul.ctrl.CellStyle.UNDERLINE_SINGLE);
//					}
//				});
				break;
			default:
				return;
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return
	 */
	private DesktopCellStyleContext getCellStyleContext() {
		return Zssapp.getDesktopCellStyleContext(self);
	}

	public void onExportFile(ForwardEvent event) {
		throw new UiException("export file not implement yet");
//		Listbox fle_files = (Listbox) Path
//				.getComponent("//p1/mainWin/fileExportWin/fle_files");
//
//		String filename = fle_files.getSelectedItem().getLabel();
//		exportFile(filename);
//
//		Window fileExportWin = (Window) Path
//				.getComponent("//p1/mainWin/fileExportWin");
//		fileExportWin.setVisible(false);
	}

	public void exportFile(String filename) {// current or other
												// files(stack_level=0)
	// TODO
		throw new UiException("export file not implmented yet");
		/*
		 * // current editing file System.out.println("exportFile filename: " +
		 * filename + ", spreadsheet source: " + spreadsheet.getSrc()); if
		 * (filename.equals(spreadsheet.getSrc())) {
		 * System.out.println("exportFile if: "); ByteArrayOutputStream
		 * baoStream = new ByteArrayOutputStream(); ExcelExporter exporter = new
		 * ExcelExporter(); exporter.exports(spreadsheet.getBook(), baoStream);
		 * byte[] bin = baoStream.toByteArray(); Filedownload.save(new
		 * ByteArrayInputStream(bin), "application/vnd.ms-excel", filename); }
		 * else {// other files System.out.println("exportFile else"); HashMap
		 * hm = fileh.readMetafile(); Object objs[] = (Object[])
		 * hm.get(filename); if (objs != null) { String hashFilename =
		 * fileh.xlsDir + ((String) objs[1]);
		 * System.out.println("hashFilename: " + hashFilename); FileInputStream
		 * iStream = null; try { iStream = new FileInputStream(hashFilename); }
		 * catch (FileNotFoundException e) { e.printStackTrace(); }
		 * Filedownload.save(iStream, "application/vnd.ms-excel", filename); } }
		 */}

	public void onPrint(ForwardEvent event) {
		throw new UiException("print not implement yet");
//		String printKey = "" + System.currentTimeMillis();
//		Session session = Executions.getCurrent().getDesktop().getSession();
//		session.setAttribute("zssFromHi" + printKey, spreadsheet);
//
//		Window win = (Window) mainWin.getFellow("menuPrintWin");
//		Button printBtn = (Button) win.getFellow("printBtn");
//		printBtn.setHref("print.zul?printKey=" + printKey);
//		printBtn.setTooltip("print.zul?printKey=" + printKey);
//
//		win.setPosition("parent");
//		win.setTop("100px");
//		win.setLeft("100px");
//		win.doPopup();
	}

	public void onRevision(ForwardEvent event) {
		throw new UiException("revision not implement yet");
//		reloadRevisionMenu();
//		Window win = (Window) mainWin.getFellow("revisionWin");
//		win.setPosition("parent");
//		win.setLeft("250px");
//		win.setTop("250px");
//		win.doPopup();
	}

	// SECTION EDIT MENU
	public void onEditUndo(ForwardEvent event) {
		throw new UiException("Undo not implmented yet");
		// spreadsheet.undo();
	}

	public void onEditRedo(ForwardEvent event) {
		throw new UiException("Redo not implemented yet");
		// spreadsheet.redo();
	}

	//TODO: move these method to compositive component
//	public void onEditCut(ForwardEvent event) {
//		EditHelper.doCut(spreadsheet);
//	}
//
//	public void onEditCopy(ForwardEvent event) {
//		EditHelper.doCopy(spreadsheet);
//	}

//	public void onEditPaste(ForwardEvent event) {
//		EditHelper.doPaste(spreadsheet);
//	}

	public void onDeleteRows(ForwardEvent event) {
		try {
			Worksheet sheet = spreadsheet.getSelectedSheet();
			Rect rect = spreadsheet.getSelection();
			int top = rect.getTop();
			int bottom = rect.getBottom();
			if (top == 0 && bottom == spreadsheet.getMaxrows() - 1) {
				try {
					Messagebox.show("cannot delete all Rows");
					return;
				} catch (InterruptedException e) {
				}
			}
			// TODO undo/redo
			// spreadsheet.pushDeleteRowColState(-1, top, -1, bottom);
			Utils.deleteRows(sheet, top, bottom);
			spreadsheet.focus();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onDeleteColumns(ForwardEvent event) {
		try {
			Worksheet sheet = spreadsheet.getSelectedSheet();
			Rect rect = spreadsheet.getSelection();
			int left = rect.getLeft();
			int right = rect.getRight();
			if (left == 0 && right == spreadsheet.getMaxcolumns() - 1) {
				try {
					Messagebox.show("cannot delete all Columns");
					return;
				} catch (InterruptedException e) {
				}
			}
			// TODO undo/redo
			// spreadsheet.pushDeleteRowColState(left, -1, right, -1);
			Utils.deleteColumns(sheet, left, right);
			spreadsheet.focus();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// SECTION FORMAT MENU
	public void onFormatNumber(ForwardEvent event) {
		Window win = (Window) mainWin.getFellow("formatNumberWin");
		win.setPosition("parent");
		win.setLeft("170px");
		win.setTop("24px");
		win.doPopup();// Modal();
	}
	
//	public void onUpload$insertImageBtn(UploadEvent evt) {
//		if (spreadsheet.getBook() != null) {
//			final Media media = evt.getMedia();
//			if (media instanceof AImage) {
//				Position p = spreadsheet.getCellFocus();
//				getWorkbookCtrl().addImage(p.getRow(), p.getColumn(), (AImage)media);
//			} else {
//				try {
//					Messagebox.show("Upload content must be image format");
//				} catch (InterruptedException e) {
//				}
//			}
//		}
//	}

	public void onInsertRows(ForwardEvent event) {
		try {
			final Rect rect = spreadsheet.getSelection();
			final int top = rect.getTop();
			final int bottom = rect.getBottom();
			// TODO undo/redo
			// spreadsheet.pushInsertRowColState(-1, top, -1, bottom);
			Utils.insertRows(spreadsheet.getSelectedSheet(), top, bottom);
			spreadsheet.focus();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onInsertColumns(ForwardEvent event) {
		try {
			Rect rect = spreadsheet.getSelection();
			int left = rect.getLeft();
			int right = rect.getRight();

			// TODO undo/redo
			// spreadsheet.pushInsertRowColState(left, -1, right, -1);
			Utils.insertColumns(spreadsheet.getSelectedSheet(), left, right);
			spreadsheet.focus();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// SECTION HELP MENU
	public void onHelpCheatsheet(ForwardEvent event) {
		Window win = (Window) mainWin.getFellow("cheatsheet");
		win.setPosition("parent");
		win.setLeft("327px");
		win.setTop("124px");
		win.doPopup();
	}

	public void onFormulaListOK() {
		Combobox formulaList = (Combobox) Path.getComponent("//p1/mainWin/formulaList");

		int left = spreadsheet.getSelection().getLeft();
		int top = spreadsheet.getSelection().getTop();
		String formula = formulaList.getSelectedItem().getLabel();
		if (formula != null) {
			final Worksheet sheet = spreadsheet.getSelectedSheet();
			Cell cell = Utils.getOrCreateCell(sheet, top, left);
			cell.setCellFormula(formula + "()");
		}
		formulaList.setText("");
	}

//	public void onMergeCellClick(ForwardEvent event) {
//		try {
//			// isMergeCell should read from the cell
//			// and search all over the cell if any one is merged then unmerge
//			// all
//			isMergeCell = !isMergeCell;
//			if (isMergeCell) {
//				mergeCellBtn.setClass("clicked");
//			}
//
//			if (isMergeCell) {
//				// TODO: undo/redo
//				// spreadsheet.pushCellState();
//				Rect sel = spreadsheet.getSelection();
//				if (sel.getLeft() == sel.getRight() && sel.getTop() == sel.getBottom()) {
//					mergeCellBtn.setClass("toolIcon");
//				} else {
//					Utils.mergeCells(spreadsheet.getSelectedSheet(), sel.getTop(), sel.getLeft(), sel.getBottom(), sel.getRight(), false);
//				}
//				spreadsheet.focus();
//			} else {
//				// TODO: undo/redo
//				// spreadsheet.pushCellState();
//				Rect sel = spreadsheet.getSelection();
//				Utils.unmergeCells(spreadsheet.getSelectedSheet(),
//						sel.getTop(), sel.getLeft(), sel.getBottom(), sel
//								.getRight());
//				spreadsheet.focus();
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}



	public void onRange(ForwardEvent event) {
		rangeh.dispatcher((String) event.getData());
	}



	public void onClick$insertPieChart(Event evt) {
		// TODO remove me, insert PieChart
		throw new UiException("insert pie chart is not implmented yet");
		/*
		 * Component cmp = execution.createComponents("piechart.zul", mainWin,
		 * null); if (cmp != null) { System.out.println("create cmp"); } Chart
		 * chart = (Chart)cmp.getFellow("mychart"); ChartWidget wgt = new
		 * ChartWidget(); wgt.setChart(chart);
		 * 
		 * int col = spreadsheet.getSelection().getLeft(); int row =
		 * spreadsheet.getSelection().getTop(); wgt.setRow(row);
		 * wgt.setColumn(col); SpreadsheetCtrl ctrl = (SpreadsheetCtrl)
		 * spreadsheet.getExtraCtrl(); ctrl.addWidget(wgt);
		 */}

	public void onInsertChart(ForwardEvent event) {
		// TODO remove me, insert chart
		throw new UiException("insert chart is not implmented yet");
		/*
		 * final int left = spreadsheet.getSelection().getLeft(); final int
		 * right = spreadsheet.getSelection().getRight(); final int top =
		 * spreadsheet.getSelection().getTop(); final int bottom =
		 * spreadsheet.getSelection().getBottom(); Sheet sheet =
		 * spreadsheet.getSelectedSheet();
		 * 
		 * final String modelType=(String)event.getData();
		 * 
		 * //BarModel model2=new SimpleBarModel(); StringBuffer strBuf = new
		 * StringBuffer();
		 * strBuf.append("<window title=\"test\" width=\"420px\" id=\"chartWin"
		 * +(chartKey++)+"\" border=\"normal\">");strBuf.append(
		 * "<chart id=\"myChart\" width=\"400px\" height=\"200px\" type=\""
		 * +modelType+"\" threeD=\"true\" fgAlpha=\"128\">");
		 * strBuf.append("<zscript>"); p(modelType);
		 * if(modelType.equals("pie")){
		 * strBuf.append("myChart.setModel(new SimplePieModel());"); }
		 * if(modelType.equals("bar")){
		 * strBuf.append("myChart.setModel(new SimpleCategoryModel());"); }
		 * strBuf.append("</zscript>"); strBuf.append("</chart>");
		 * strBuf.append("</window>"); Window win= (Window)
		 * Executions.createComponentsDirectly(strBuf.toString(), null, mainWin,
		 * null); Chart myChart = (Chart) win.getFellow("myChart"); ChartModel
		 * model=myChart.getModel(); for(int row=top; row<=bottom; row++){ Cell
		 * cellName=sheet.getCell(row, left); String name; if(cellName==null ||
		 * cellName.getText()==null) name=""; else name=cellName.getText();
		 * for(int col=left+1; col<=right; col++){ Cell
		 * cellValue=sheet.getCell(row, col); double value; if(cellValue==null
		 * || cellValue.getResult()==null) value=0; else{ try{
		 * value=((Double)cellValue.getResult()).doubleValue(); }catch(Exception
		 * e){ value=0; }
		 * 
		 * //p(name); //p(""+value); if(modelType.equals("pie")){
		 * ((PieModel)model).setValue((Comparable)name,new Double(value)); }
		 * if(modelType.equals("bar")){
		 * ((CategoryModel)model).setValue((Comparable)name, (Comparable)new
		 * Long(col-left), new Double(value)); } } } }
		 * if(modelType.equals("pie")) myChart.setModel((PieModel)model);
		 * if(modelType.equals("bar")) myChart.setModel((CategoryModel)model);
		 * 
		 * win.doOverlapped(); //win.setSizable(true); win.setClosable(true);
		 * //win.setAttribute(SpreadsheetCtrl.CHILD_PASSING_KEY,"");
		 * //spreadsheet.appendChild(win); mainWin.appendChild(win);
		 * 
		 * win.setLeft("100px"); win.setTop("200px"); win.doOverlapped();
		 * //spreadsheet.smartUpdate("appendWidget",win.getUuid());
		 * 
		 * 
		 * 
		 * 
		 * //Add EventListener for updating chart
		 * myChart.addEventListener(Events.ON_STOP_EDITING, new EventListener()
		 * { public void onEvent(Event event) throws Exception { int
		 * modRow=((CellEvent)event).getRow(); int
		 * modCol=((CellEvent)event).getColumn(); if(left<=modCol &&
		 * modCol<=right && top<=modRow && modRow<=bottom){
		 * //p("ChartValue Editing:"+(String)event.getData());
		 * //p("row: "+top+" "+bottom); //p("col: "+left+" "+right);
		 * //p("event:"+modelType); Sheet sheet=((CellEvent)event).getSheet();
		 * Chart myChart=(Chart)event.getTarget(); ChartModel model =
		 * myChart.getModel(); if(modelType.equals("pie"))
		 * ((PieModel)model).clear(); if(modelType.equals("bar"))
		 * ((CategoryModel)model).clear();
		 * 
		 * for(int row=top; row<=bottom; row++){ Cell
		 * cellName=sheet.getCell(row, left); String name; if(cellName==null ||
		 * cellName.getText()==null) name=""; else name=cellName.getText();
		 * 
		 * for(int col=left+1; col<=right; col++){ Cell
		 * cellValue=sheet.getCell(row, col); double value; if(cellValue==null
		 * || cellValue.getResult()==null) value=0; else{ try{
		 * value=((Double)cellValue.getResult()).doubleValue(); }catch(Exception
		 * e){ value=0; }
		 * 
		 * //p(name); //p(""+value); if(modelType.equals("pie")){
		 * ((PieModel)model).setValue((Comparable)name,new Double(value)); }
		 * if(modelType.equals("bar")){
		 * ((CategoryModel)model).setValue((Comparable)name, (Comparable)new
		 * Long(col-left), new Double(value)); } } } }
		 * if(modelType.equals("pie")) myChart.setModel((PieModel)model);
		 * if(modelType.equals("bar")) myChart.setModel((CategoryModel)model);
		 * 
		 * }
		 * 
		 * } });
		 * 
		 * win.addEventListener(org.zkoss.zk.ui.event.Events.ON_CLICK, new
		 * EventListener(){
		 * 
		 * public void onEvent(Event event) throws Exception { Rect rect=new
		 * Rect(); rect.setBottom(bottom); rect.setTop(top);
		 * rect.setRight(right); rect.setLeft(left);
		 * spreadsheet.setSelection(rect); } });
		 */
	}

	public void reloadRevisionMenu() {
		throw new UiException("reloadRevision not implement yet");
//		try {
//			// read the metafile
//			BufferedReader bReader;
//			bReader = new BufferedReader(new FileReader(fileh.xlsDir
//					+ "metaFile"));
//			String line = null;
//			Stack stack = new Stack();
//
//			String timeStr, filename, hashFilename;
//			while (true) {
//				timeStr = bReader.readLine();
//				if (timeStr == null) {
//					break;
//				}
//				filename = bReader.readLine();
//				if (filename == null) {
//					System.out.println("Warning: filename cannot read from metaFile");
//					break;
//				}
//				hashFilename = bReader.readLine();
//				if (hashFilename == null) {
//					System.out.println("Warning: hashFilename cannot read from metaFile");
//					break;
//				}
//				if (spreadsheet.getSrc().equals(filename)) {
//					stack.add(timeStr);
//					stack.add(filename);
//					stack.add(hashFilename);
//				}
//			}
//			bReader.close();
//
//			// put read data to Menu
//			org.zkoss.zul.Row newRow;
//			Rows revisionRows = (Rows) mainWin.getFellow("revisionWin")
//					.getFellow("revisionRows");
//			List rowsChildList = revisionRows.getChildren();
//			while (!rowsChildList.isEmpty())
//				revisionRows.removeChild((Component) rowsChildList.get(0));
//			while (!stack.isEmpty()) {
//
//				hashFilename = (String) stack.pop();
//				filename = (String) stack.pop();
//				timeStr = (String) stack.pop();
//				String dateStr = new Date(Long.parseLong(timeStr)).toString();
//
//				newRow = new org.zkoss.zul.Row();
//				Radio radio = new Radio();
//				radio.setAttribute("value", hashFilename);
//
//				if (hashFilename.equals(this.hashFilename)) {
//					newRow.appendChild(new Label("current"));
//					newRow.setStyle("background:rgb(250,230,180) none");
//				} else {
//					newRow.appendChild(radio);
//					// p("set hashFilename: "+hashFilename);
//				}
//				newRow.appendChild(new Label(dateStr));
//				newRow.appendChild(new Label("test name"));
//				newRow.appendChild(new Label("no comment"));
//
//				revisionRows.appendChild(newRow);
//				// p(""+radio.getAttribute("value"));
//			}
//			// close the jdbc connection
//
//		} catch (Exception ex) {
//			throw new RuntimeException(ex);
//		}
	}

//	public void onRevisionOK(ForwardEvent event) {
//		String filename = null;
//
//		Rows revisionRows = (Rows) mainWin.getFellow("revisionWin").getFellow(
//				"revisionRows");
//		List rowList = revisionRows.getChildren();
//		for (int i = 0; i < rowList.size(); i++) {
//			Component tmpComponent = ((Component) rowList.get(i))
//					.getFirstChild();
//			if (tmpComponent instanceof Radio
//					&& ((Radio) tmpComponent).isChecked()) {
//				filename = (String) ((Radio) tmpComponent)
//						.getAttribute("value");
//				// p("onRevision: "+filename);
//			}
//		}
//
//		openFileInFS(filename);
//
//		Window revisionWin = (Window) Path
//				.getComponent("//p1/mainWin/revisionWin");
//		revisionWin.setVisible(false);
//		spreadsheet.invalidate();
//		// spreadsheet.notifyRevision();
//	}

//	private Connection getDBConnection() {
//		try {
//			return DriverManager.getConnection(
//					"jdbc:mysql://localhost:3306/zss", "root", "rootzk");
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}

	public void onNotImplement(ForwardEvent event) {
		try {
			Messagebox.show("Not implement yet");
		} catch (InterruptedException e) {
		}
	}
	
	public void onCheck$gridlinesCheckbox() {
		Worksheet sheet = spreadsheet.getSelectedSheet();
		Utils.getRange(sheet, 0, 0).setDisplayGridlines(!sheet.isDisplayGridlines());
		getDesktopWorkbenchContext().getWorkbookCtrl().reGainFocus();
	}
	
	public void onCheck$protectSheet() {
//		getDesktopWorkbenchContext().getWorkbookCtrl().protectSheet(protectSheet.isChecked() ? "" : null);
	}

	public void openCustomSortDialog(Rect selection) {
		if (_customSortDialog == null || _customSortDialog.isInvalidated())
			_customSortDialog = (Dialog) Executions.createComponents(Consts._CustomSortDialog_zul, mainWin, CustomSortWindowCtrl.newArg(spreadsheet));
		_customSortDialog.fireOnOpen(selection);
	}
	
	public void openHyperlinkDialog(Rect selection) {
		if (_insertHyperlinkDialog == null || _insertHyperlinkDialog.isInvalidated())
			_insertHyperlinkDialog = (Dialog)Executions.createComponents(Consts._InsertHyperlinkDialog_zul, mainWin, Zssapps.newSpreadsheetArg(spreadsheet));
		_insertHyperlinkDialog.fireOnOpen(selection);
	}

	public void openInsertFormulaDialog() {
		if (_insertFormulaDialog == null || _insertFormulaDialog.isInvalidated())
			_insertFormulaDialog = (Dialog)Executions.createComponents(Consts._InsertFormulaDialog2_zul, mainWin, null);
		_insertFormulaDialog.fireOnOpen(null);
	}

	public void openPasteSpecialDialog() {
		if (_pasteSpecialDialog == null || _pasteSpecialDialog.isInvalidated())
			_pasteSpecialDialog = (Dialog)Executions.createComponents(Consts._PasteSpecialDialog_zul, mainWin, Zssapps.newSpreadsheetArg(spreadsheet));
		_pasteSpecialDialog.fireOnOpen(null);
	}

	public boolean toggleFormulaBar() {
		spreadsheet.setShowFormulabar(!spreadsheet.isShowFormulabar());
		return spreadsheet.isShowFormulabar();
	}

	public void openComposeFormulaDialog(FormulaMetaInfo metainfo) {
		if (_composeFormulaDialog == null || _composeFormulaDialog.isInvalidated())
			_composeFormulaDialog = (Dialog) Executions.createComponents(Consts._ComposeFormulaDialog_zul, mainWin, arg);
		_composeFormulaDialog.fireOnOpen(metainfo);
	}

	public void openFormatNumberDialog(Rect selection) {
		if (_formatNumberDialog == null || _formatNumberDialog.isInvalidated())
			_formatNumberDialog = (Dialog) Executions.createComponents(Consts._FormatNumberDialog_zul, mainWin, Zssapps.newSpreadsheetArg(spreadsheet));
		_formatNumberDialog.fireOnOpen(selection);	
	}

	public void openSaveFileDialog() {
		if (_saveFileDialog == null || _saveFileDialog.isInvalidated())
			_saveFileDialog = (Dialog) Executions.createComponents(Consts._SaveFile_zul, mainWin, null);
		_saveFileDialog.fireOnOpen(null);
	}

	public void openModifyHeaderSizeDialog(int headerType, Rect selection) {
		int prev = -1;
		boolean sameVal = true;
		if (headerType == WorkbookCtrl.HEADER_TYPE_ROW) {
			for (int i = selection.getTop(); i <= selection.getBottom(); i++) {
				if (prev < 0)
					prev = Utils.getRowHeightInPx(spreadsheet.getSelectedSheet(), i);
				else if (prev != Utils.getRowHeightInPx(spreadsheet.getSelectedSheet(), i)) {
					sameVal = false;
					break;
				}
			}
		} else {
			for (int i = selection.getLeft(); i <= selection.getRight(); i++) {
				if (prev < 0)
					prev = Utils.getColumnWidthInPx(spreadsheet.getSelectedSheet(), i);
				else if (prev != Utils.getColumnWidthInPx(spreadsheet.getSelectedSheet(), i)) {
					sameVal = false;
					break;
				}
			}
		}

		if (_headerSizeDialog == null || _headerSizeDialog.isInvalidated())
			_headerSizeDialog = (Dialog) Executions.createComponents(Consts._HeaderSize_zul, mainWin, null);
		_headerSizeDialog.fireOnOpen(
			HeaderSizeCtrl.newArg(Integer.valueOf(headerType), sameVal ? prev : null, selection));
	}

	public void openRenameSheetDialog(String originalSheetName) {
		if (_renameSheetDialog == null || _renameSheetDialog.isInvalidated())
			_renameSheetDialog = (Dialog) Executions.createComponents(Consts._RenameDialog_zul, mainWin, null);
		_renameSheetDialog.fireOnOpen(RenameSheetCtrl.newArg(originalSheetName));
	}

	public void openOpenFileDialog() {
		if (_openFileDialog == null || _openFileDialog.isInvalidated())
			_openFileDialog = (Dialog) Executions.createComponents(Consts._OpenFile_zul, mainWin, null);
		_openFileDialog.fireOnOpen(null);
	}

	public void openImportFileDialog() {
		if (_importFileDialog == null || _importFileDialog.isInvalidated())
			_importFileDialog = (Dialog) Executions.createComponents(Consts._ImportFile_zul, mainWin, null);
		_importFileDialog.fireOnOpen(null);
	}
	
	public void openExportPdfDialog(Rect selection) {
		if (!hasZssPdf()) {
			try {
				Messagebox.show("Please download Zss Pdf from ZK");
			} catch (InterruptedException e) {
			}
			return;
		}
		if (_exportToPdfDialog == null || _exportToPdfDialog.isInvalidated())
			_exportToPdfDialog = (Dialog) Executions.createComponents(Consts._ExportToPDF_zul, mainWin, Zssapps.newSpreadsheetArg(spreadsheet));
		_exportToPdfDialog.fireOnOpen(selection);
	}
		
	private static boolean hasZssPdf() {
		String val = Library.getProperty(KEY_PDF);
		if (val == null) {
			boolean hasZssPdf = verifyZssPdf();
			Library.setProperty(KEY_PDF, String.valueOf(hasZssPdf));
			return hasZssPdf;
		} else {
			return Boolean.valueOf(Library.getProperty(KEY_PDF));
		}
	}

	/**
	 * Verify whether has zss pdf export function or not
	 */
	private static boolean verifyZssPdf() {
		try {
			Class.forName("org.zkoss.zss.model.impl.pdf.PdfExporter");
		} catch (ClassNotFoundException ex) {
			return false;
		}
		return true;
	}

	//TODO: mimic openExportPdfDialog()
	@Override
	public void openExportHtmlDialog(Rect selection) {
		if (!hasZssHtml()) {
			try {
				Messagebox.show("Please download Zss Html from ZK");
			} catch (InterruptedException e) {
			}
			return;
		}
		if (_exportToHtmlDialog == null || _exportToHtmlDialog.isInvalidated())
			_exportToHtmlDialog = (Dialog) Executions.createComponents(Consts._ExportToHTML_zul, mainWin, Zssapps.newSpreadsheetArg(spreadsheet));
		_exportToHtmlDialog.fireOnOpen(null);
		
	}

	private static boolean hasZssHtml() {
		String val = Library.getProperty(KEY_HTML);
		if (val == null) {
			boolean hasZssHtml = verifyZssHtml();
			Library.setProperty(KEY_HTML, String.valueOf(hasZssHtml));
			return hasZssHtml;
		} else {
			return Boolean.valueOf(Library.getProperty(KEY_HTML));
		}
	}

	private static boolean verifyZssHtml() {
		try {
			Class.forName("org.zkoss.zss.model.impl.html.HtmlExporter");
		} catch (ClassNotFoundException ex) {
			return false;
		}
		return true;
	}
	
	private class MainActionHandler extends ActionHandler {
		
		MainActionHandler() {
			super(spreadsheet);
		}

		@Override
		public void doNewBook() {
			getDesktopWorkbenchContext().getWorkbookCtrl().newBook();
			getDesktopWorkbenchContext().fireWorkbookChanged();
		}

		@Override
		public void doSaveBook() {
			if (spreadsheet.getBook() != null) {
				DesktopWorkbenchContext workbench = getDesktopWorkbenchContext();
				if (workbench.getWorkbookCtrl().hasFileExtentionName()) {
					workbench.getWorkbookCtrl().save();
					workbench.fireWorkbookSaved();
				} else
					workbench.getWorkbenchCtrl().openSaveFileDialog();	
			}
		}

		@Override
		public void doExportPDF(Rect selection) {
			if (spreadsheet.getBook() != null && validSelection(selection)) {
				openExportPdfDialog(selection);	
			}
		}

		@Override
		public void doPasteSpecial(Rect selection) {
			if (spreadsheet.getSelectedSheet() != null && validSelection(selection)) {
				spreadsheet.setSelection(selection);
				openPasteSpecialDialog();	
			}
		}

		@Override
		public void doFontSize(int fontSize, Rect selection) {
			super.doFontSize(fontSize, selection);
			
			//For performance reason, markout this behavior. (when select 2 columns and set font size)
			//TODO: improve client side performance for this behavior
			//setProperRowHeightByFontSize(spreadsheet.getSelectedSheet(), selection, fontSize);
		}
		
		private void setProperRowHeightByFontSize(Worksheet sheet, Rect rect, int size) {	
			int tRow = rect.getTop();
			int bRow = rect.getBottom();
			int col = rect.getLeft();
			
			for (int i = tRow; i <= bRow; i++) {
				//Note. add extra padding height: 4
				if ((size + 4) > (Utils.pxToPoint(Utils.twipToPx(BookHelper.getRowHeight(sheet, i))))) {
					Ranges.range(sheet, i, col).setRowHeight(size + 4);
				}
			}
		}

		@Override
		public void doCustomSort(Rect selection) {
			if (spreadsheet.getSelectedSheet() != null && validSelection(selection)) {
				openCustomSortDialog(selection);
			}
		}

		@Override
		public void doHyperlink(Rect selection) {
			if (spreadsheet.getSelectedSheet() != null && validSelection(selection)) {
				openHyperlinkDialog(selection);
			}
		}

		@Override
		public void doFormatCell(Rect selection) {
			if (spreadsheet.getSelectedSheet() != null && validSelection(selection)) {
				openFormatNumberDialog(selection);	
			}
		}

		@Override
		public void doColumnWidth(Rect selection) {
			if (spreadsheet.getSelectedSheet() != null && validSelection(selection)) {
				openModifyHeaderSizeDialog(WorkbookCtrl.HEADER_TYPE_COLUMN, selection);	
			}
		}

		@Override
		public void doRowHeight(Rect selection) {
			if (spreadsheet.getSelectedSheet() != null && validSelection(selection)) {
				openModifyHeaderSizeDialog(WorkbookCtrl.HEADER_TYPE_ROW, selection);	
			}
		}
	}
}
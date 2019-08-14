/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kirjanpito.models;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.swing.SwingWorker;
import kirjanpito.db.Account;
import kirjanpito.db.DTOCallback;
import kirjanpito.db.DataAccessException;
import kirjanpito.db.DataSource;
import kirjanpito.db.Document;
import kirjanpito.db.DocumentDAO;
import kirjanpito.db.Entry;
import kirjanpito.db.EntryDAO;
import kirjanpito.db.Period;
import kirjanpito.db.Session;
import kirjanpito.util.CSVWriter;
import kirjanpito.util.Registry;

/**
 *
 * @author ville
 */
public class CSVImportWorker extends SwingWorker<Void, Void> {
	private Registry registry;
	private File file;
	private SimpleDateFormat dateFormat;
	private DecimalFormat numberFormat;
	private IOException exception;
	
	public CSVImportWorker(Registry registry, File file) {
		this.registry = registry;
		this.file = file;
		dateFormat = new SimpleDateFormat("d.M.yyyy");
		numberFormat = new DecimalFormat();
		numberFormat.setMinimumFractionDigits(2);
		numberFormat.setMaximumFractionDigits(2);
	}
	
	protected Void doInBackground() throws Exception {
		DataSource dataSource = registry.getDataSource();
		Session sess = null;
		
		try {
			sess = dataSource.openSession();
			//export(sess);
                        importFromFile(sess);
			sess.commit();
		}
		catch (Exception e) {
			e.printStackTrace();
			if (sess != null) sess.rollback();
			throw e;
		}
		finally {
			sess.close();
		}
		
		return null;
	}
        
        private void importFromFile(Session sess) throws IOException,
		DataAccessException {
		
		DataSource dataSource = registry.getDataSource();
		Period period = registry.getPeriod();
		
                if (period.isLocked()) {
			throw new RuntimeException("Tilikausi on lukittu");
		}
                Document newDocument ;
                //newDocument.setDate(Date.from(Instant.now()));
		EntryDAO entryDAO;
                Entry e1 = new Entry();
		try {
			sess = dataSource.openSession();
			entryDAO = dataSource.getEntryDAO(sess);
                        DocumentDAO d =dataSource.getDocumentDAO(sess);
			
                        newDocument = d.create(1, 0, 99);
                        newDocument.setDate(Date.from(Instant.now()));
                        d.save(newDocument);
                        
			
			
                        //e1.setId(13);//viennin tunniste
                        e1.setDocumentId(newDocument.getId());//tositteen tunniste
                        e1.setAccountId(47); //tilin tunniste
                        e1.setDebit(true);
                        e1.setAmount(BigDecimal.ONE);
                        e1.setDescription("testisyöttö41");
                        e1.setRowNumber(1);
                        e1.setFlags(0);
                        entryDAO.save(e1);
			

			sess.commit();
		}
		catch (DataAccessException e) {
			if (sess != null) sess.rollback();
			throw e;
		}
		finally {
			if (sess != null) sess.close();
		}
	}
}
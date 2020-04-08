/*
 * Copyright 2009-2020 Contributors (see credits.txt)
 *
 * This file is part of jEveAssets.
 *
 * jEveAssets is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * jEveAssets is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jEveAssets; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
package net.nikr.eve.jeveasset.io.local;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.filechooser.FileSystemView;
import net.nikr.eve.jeveasset.data.api.raw.RawPublicMarketOrder;
import net.nikr.eve.jeveasset.gui.shared.Formater.DateFormatThreadSafe;
import net.nikr.eve.jeveasset.gui.tabs.orders.MarketLog;
import net.nikr.eve.jeveasset.gui.tabs.orders.OutbidProcesser;
import net.nikr.eve.jeveasset.gui.tabs.orders.OutbidProcesser.OutbidProcesserInput;
import net.nikr.eve.jeveasset.gui.tabs.orders.OutbidProcesser.OutbidProcesserOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseBool;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.ParseLong;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.ift.StringCellProcessor;
import org.supercsv.exception.SuperCsvCellProcessorException;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.CsvContext;

public class MarketLogReader {

	private static final Logger LOG = LoggerFactory.getLogger(OutbidProcesser.class);

	private static final int RETRIES = 3;
	private static final Set<String> PARSED_FILES = Collections.synchronizedSet(new HashSet<>());

	DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd hhmmss");
	private final OutbidProcesserInput input;

	public MarketLogReader(OutbidProcesserInput input) {
		this.input = input;
	}

	public static void markOld() {
		File marketlogsDirectory = getMarketlogsDirectory();
		if (!marketlogsDirectory.exists()) {
			return;
		}
		for (File file : marketlogsDirectory.listFiles()) {
			PARSED_FILES.add(file.getName());
		}
	}

	public static List<MarketLog> read(File file, OutbidProcesserInput input, OutbidProcesserOutput output) {
		MarketLogReader reader = new MarketLogReader(input);
		List<MarketLog> orders = reader.read(file);
		OutbidProcesser.process(input, output);
		return orders;
	}

	private List<MarketLog> read(final File logFile) {
		final String filename = logFile.getName();
		if (PARSED_FILES.contains(filename)) {
			LOG.info("Old file ignored: " + filename);
			return null;
		} else {
			PARSED_FILES.add(filename);
		}
		LOG.info("Reading: " + filename);
		String[] values = filename.split("-");
		if (values.length < 3) {
			return null;
		}
		Date date;
		try {
			date = dateFormat.parse(values[values.length-1]);
		} catch (ParseException ex) {
			LOG.error(ex.getMessage(), ex);
			return null;
		}
		
		List<MarketLog> marketLogs = parse(logFile);
		if (marketLogs == null || marketLogs.isEmpty()) {
			LOG.warn("No orders found in: " + filename);
			return null;
		}
		Integer typeID = marketLogs.get(0).getTypeID();
		if (typeID == null) {
			LOG.warn("typeID is null: " + filename);
			return null; 
		}
		List<RawPublicMarketOrder> marketOrders = new ArrayList<>();
		for (MarketLog marketLog : marketLogs) {
			marketOrders.add(new RawPublicMarketOrder(marketLog));
		}
		Map<Integer, List<RawPublicMarketOrder>> orders = new HashMap<>();
		orders.put(typeID, marketOrders);
		input.addOrders(orders, date);
		LOG.info("File data added: " + filename);
		return marketLogs;
	}

	private List<MarketLog> parse(File file) {
		return parse(file, 0);
	}

	private List<MarketLog> parse(File file, int retries) {
		Reader reader = null;
		ICsvBeanReader beanReader = null;
		try {
			beanReader = new CsvBeanReader(new FileReader(file), CsvPreference.STANDARD_PREFERENCE);
			beanReader.getHeader(true);
			// the header elements are used to map the values to the bean (names must match)
			final String[] header = {"price","volRemaining","typeID","range","orderID","volEntered","minVolume","bid","issueDate","duration","stationID","regionID","solarSystemID","jumps", "empty"};

			List<MarketLog> marketLogs = new ArrayList<>();
			MarketLog marketLog;
			while ((marketLog = beanReader.read(MarketLog.class, header, getProcessors())) != null) {
				marketLogs.add(marketLog);
			}
			return marketLogs;
		} catch (IllegalArgumentException ex) {
			if (retries > 3) {
				LOG.error(ex.getMessage(), ex);
				return null;
			}
		} catch (IOException ex) {
			LOG.error(ex.getMessage(), ex);
			return null;
		} finally {
			if (beanReader != null) {
				try {
					beanReader.close();
				} catch (IOException ex) {
					//No problem
				}
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ex) {
					//No problem
				}
			}
		}
		retries++;
		LOG.info("Retrying in " + 500 * retries +  "ms (" + retries + " of " + RETRIES + ")");
		try {
			Thread.sleep(500 * retries);
		} catch (InterruptedException ex1) {
			//No problem, just continue
		}
		return parse(file, retries);
	}

	private static CellProcessor[] getProcessors() {
		return new CellProcessor[]{
			new ParseDouble(), // price
			new ParseDouble(), // volRemaining
			new ParseInt(), // typeID
			new ParseInt(), // range
			new ParseLong(), // orderID
			new ParseInt(), // volEntered
			new ParseInt(), // minVolume
			new ParseBool(), // bid
			new ParseDate(), // issueDate
			new ParseInt(), // duration
			new ParseLong(), // stationID
			new ParseLong(), // regionID
			new ParseLong(), // solarSystemID
			new ParseInt(), // jumps
			new Optional()
		};
	}

	public static File getMarketlogsDirectory() {
		//https://wiki.eveuniversity.org/EVE_logs
		File documents = FileSystemView.getFileSystemView().getDefaultDirectory();
		String home = System.getProperty("user.home"); // can be null
		StringBuilder builder = new StringBuilder();
		if (documents.exists()) {
			System.out.println("documents: " + documents.getAbsolutePath());
			builder.append(documents.getPath());
		} else {
			System.out.println("home: " + home);
			builder.append(home);
		}
		if (!new File(documents.getAbsolutePath() + File.separator + "EVE").exists()
				&& new File(documents.getAbsolutePath() + File.separator + "Documents").exists()) {
			builder.append(File.separator);
			builder.append("Documents");
		}
		builder.append(File.separator);
		builder.append("EVE");
		builder.append(File.separator);
		builder.append("logs");
		builder.append(File.separator);
		builder.append("Marketlogs");
		return new File(builder.toString());
	}

	@SuppressWarnings("unchecked")
	public static class ParseDate extends CellProcessorAdaptor implements StringCellProcessor {

		public static final DateFormatThreadSafe DATETIME = new DateFormatThreadSafe("yyyy-MM-dd hh:mm:ss", true);
		public static final DateFormatThreadSafe DATE = new DateFormatThreadSafe("yyyy-MM-dd", true);

		public ParseDate() {
			super();
		}

		public ParseDate(CellProcessor next) {
			// this constructor allows other processors to be chained after ParseDay
			super(next);
		}

		@Override
		public Object execute(Object value, CsvContext context) {
			if (value == null) {
				throw new SuperCsvCellProcessorException(
						"this processor does not accept null input - if the column is optional then chain an Optional() processor before this one",
						context, this);
			}

			Date result;
			if (value instanceof Date) {
				result = (Date) value;
			} else if (value instanceof String) {
				try {
					result = DATETIME.parse((String)value);
				} catch (ParseException ex) {
					try {
						result = DATE.parse((String)value);
					} catch (ParseException ex2) {
						throw new SuperCsvCellProcessorException(
								String.format("'%s' could not be parsed as an Date", value), context, this, ex2);

					}
				}
			} else {
				final String actualClassName = value.getClass().getName();
				throw new SuperCsvCellProcessorException(String.format(
						"the input value should be of type Date or String but is of type %s", actualClassName), context,
						this);
			}
			return next.execute(result, context);
		}
	}
}

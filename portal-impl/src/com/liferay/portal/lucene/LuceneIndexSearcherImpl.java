/**
 * Copyright (c) 2000-2008 Liferay, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.liferay.portal.lucene;

import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.IndexSearcher;
import com.liferay.portal.kernel.search.Query;
import com.liferay.portal.kernel.search.SearchEngineUtil;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.util.search.QueryImpl;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;

/**
 * <a href="LuceneIndexSearcherImpl.java.html"><b><i>View Source</i></b></a>
 *
 * @author Bruno Farache
 *
 */
public class LuceneIndexSearcherImpl implements IndexSearcher {

	public Hits search(long companyId, Query query, int begin, int end)
		throws SearchException {

		LuceneHitsImpl hits = new LuceneHitsImpl();

		org.apache.lucene.search.IndexSearcher searcher = null;

		try {
			searcher = LuceneUtil.getSearcher(companyId);

			// LEP-5958

			if (query instanceof QueryImpl) {
				hits.recordHits(
					searcher.search(((QueryImpl)query).getQuery()), searcher);
			}
			else {
				QueryParser parser = new QueryParser(
					StringPool.BLANK, LuceneUtil.getAnalyzer());

				hits.recordHits(
					searcher.search(parser.parse(query.parse())), searcher);
			}

			if ((begin == SearchEngineUtil.ALL_POS) &&
				(end == SearchEngineUtil.ALL_POS)) {

				hits = hits.subset(0, hits.getLength());
			}
			else {
				hits = hits.subset(begin, end);
			}
		}
		catch (Exception e) {
			if (e instanceof BooleanQuery.TooManyClauses ||
				e instanceof ParseException) {

				_log.error("Parsing keywords " + query.parse(), e);

				return new LuceneHitsImpl();
			}
			else {
				throw new SearchException(e);
			}
		}
		finally {
			try {
				if (searcher != null) {
					searcher.close();

					hits.setSearcher(null);
				}
			}
			catch (IOException ioe) {
				throw new SearchException(ioe);
			}
		}

		return hits;
	}

	private static Log _log = LogFactory.getLog(LuceneHitsImpl.class);

}
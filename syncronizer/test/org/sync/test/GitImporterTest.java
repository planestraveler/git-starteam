/*****************************************************************************
	This file is part of Git-Starteam.

	Git-Starteam is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	Git-Starteam is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with Git-Starteam.  If not, see <http://www.gnu.org/licenses/>.
******************************************************************************/
package org.sync.test;

import static org.junit.Assert.*;

import static org.hamcrest.CoreMatchers.equalTo;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.sync.GitImporter;

public class GitImporterTest {
	@Rule
	public ErrorCollector collector = new ErrorCollector();

	@Test
	public void testRefName() {
		String[][] tests = {
			// rule 1
			{"a/.b", "a/_b"},
			{"a/b.lock", "a/b_lock"},
			// rule 3
			{"..a", "__a"},
			{"a..", "a__"},
			{"a..b", "a__b"},
			// rule 4
			{"\000\010\020\030\040 ~^:", "_________"},
			// rule 5
			{"?*[", "___"},
			// rule 6
			{"/a", "a"},
			{"//a", "a"},
			{"a/", "a"},
			{"a//", "a"},
			{"/a/", "a"},
			{"//a//", "a"},
			{"a///b", "a/b"},
			{"a///b", "a/b"},
			{"a///b///c", "a/b/c"},
			// rule 7
			{"a.", "a_"},
			// rule 8
			{"@{", "__"},
			{"a@{", "a__"},
			{"@{a", "__a"},
			// rule 9
			{"a\\.b", "a_.b"},
			// real label test
			{"119_ BootesOchre.114.00", "119__BootesOchre.114.00"},
			{"\"This is a quoted String\"", "_This_is_a_quoted_String_"},
		};
    for (String[] test1 : tests) {
      String test = test1[0];
      String want = test1[1];
      String got = GitImporter.refName(test);
      Matcher<String> comparator = equalTo(want);
      assertEquals(want, got);
    }
	}

}

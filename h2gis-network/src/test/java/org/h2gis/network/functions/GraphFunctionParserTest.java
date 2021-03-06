/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; 
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.network.functions;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

import static junit.framework.Assert.assertTrue;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.h2gis.utilities.TableLocation;
import org.h2gis.utilities.TableUtilities;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the parsing methods of {@link org.h2gis.network.graph_creator.GraphFunctionParser}.
 *
 * @author Adam Gouge
 * @author Erwan Bocher
 */
public class GraphFunctionParserTest {

    private static GraphFunctionParser parser;
    private static Connection connection;

    @BeforeClass
    public static void setUp() throws Exception {
        parser = new GraphFunctionParser();
        connection = H2GISDBFactory.createSpatialDataBase(GraphFunctionParserTest.class.getSimpleName());
    }

    @AfterClass
    public static void tearDown() throws Exception {
        connection.close();
    }

    private void checkOrientation(String input, GraphFunctionParser.Orientation global, String local) {
        assertEquals(global, parser.parseGlobalOrientation(input));
        assertEquals(local, parser.parseEdgeOrientation(input));
    }

    @Test
    public void testNonNullOrientations() {
        checkOrientation("directed - edge_orientation", GraphFunctionParser.Orientation.DIRECTED, "edge_orientation");
        checkOrientation("directed- edge_orientation", GraphFunctionParser.Orientation.DIRECTED, "edge_orientation");
        checkOrientation("directed-edge_orientation", GraphFunctionParser.Orientation.DIRECTED, "edge_orientation");
        checkOrientation(" directed-edge_orientation   ", GraphFunctionParser.Orientation.DIRECTED, "edge_orientation");
        checkOrientation("diRecTEd - name", GraphFunctionParser.Orientation.DIRECTED, "name");
        checkOrientation("rEVersEd - rname", GraphFunctionParser.Orientation.REVERSED, "rname");
        checkOrientation("UnDiRecTEd - q w98 er2", GraphFunctionParser.Orientation.UNDIRECTED, "q w98 er2");
    }

    @Test
    public void testNullOrientation() {
        assertEquals(null, parser.parseGlobalOrientation(null));
        assertEquals(null, parser.parseEdgeOrientation(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGlobalOrientationSpellingError() {
        parser.parseGlobalOrientation("undirrected");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingEdgeOrientationError() {
        parser.parseEdgeOrientation("directed");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEdgeOrientationUndirectedGraphError() {
        parser.parseEdgeOrientation("undirected");
    }

    @Test
    public void testNonNullWeights() {
        assertEquals("weight", parser.parseWeight("weight"));
        assertEquals("weight", parser.parseWeight("  weight     "));
        assertEquals("weight_column", parser.parseWeight("weight_column"));
        assertEquals("weight column", parser.parseWeight("weight column"));
    }

    @Test
    public void testNullWeight() {
        assertEquals(null, parser.parseWeight(null));
    }

    @Test
    public void testWeightOrientationParser() {
        // DO
        GraphFunctionParser p = new GraphFunctionParser();
        p.parseWeightAndOrientation("directed - edge_orientation", null);
        checkWeightAndOrientation(p, null, GraphFunctionParser.Orientation.DIRECTED, "edge_orientation");
        p = new GraphFunctionParser();
        p.parseWeightAndOrientation(null, "directed - edge_orientation");
        checkWeightAndOrientation(p, null, GraphFunctionParser.Orientation.DIRECTED, "edge_orientation");
        // RO
        p = new GraphFunctionParser();
        p.parseWeightAndOrientation("reversed - edge_orientation", null);
        checkWeightAndOrientation(p, null, GraphFunctionParser.Orientation.REVERSED, "edge_orientation");
        p = new GraphFunctionParser();
        p.parseWeightAndOrientation(null, "reversed - edge_orientation");
        checkWeightAndOrientation(p, null, GraphFunctionParser.Orientation.REVERSED, "edge_orientation");
        // U
        p = new GraphFunctionParser();
        p.parseWeightAndOrientation("undirected", null);
        checkWeightAndOrientation(p, null, GraphFunctionParser.Orientation.UNDIRECTED, null);
        p = new GraphFunctionParser();
        p.parseWeightAndOrientation(null, "undirected");
        checkWeightAndOrientation(p, null, GraphFunctionParser.Orientation.UNDIRECTED, null);
        // WDO
        p = new GraphFunctionParser();
        p.parseWeightAndOrientation("weight", "directed - edge_orientation");
        checkWeightAndOrientation(p, "weight", GraphFunctionParser.Orientation.DIRECTED, "edge_orientation");
        p = new GraphFunctionParser();
        p.parseWeightAndOrientation("directed - edge_orientation", "weight");
        checkWeightAndOrientation(p, "weight", GraphFunctionParser.Orientation.DIRECTED, "edge_orientation");
        // WRO
        p = new GraphFunctionParser();
        p.parseWeightAndOrientation("weight", "reversed - edge_orientation");
        checkWeightAndOrientation(p, "weight", GraphFunctionParser.Orientation.REVERSED, "edge_orientation");
        p = new GraphFunctionParser();
        p.parseWeightAndOrientation("reversed - edge_orientation", "weight");
        checkWeightAndOrientation(p, "weight", GraphFunctionParser.Orientation.REVERSED, "edge_orientation");
        // WU
        p = new GraphFunctionParser();
        p.parseWeightAndOrientation("weight", "undirected");
        checkWeightAndOrientation(p, "weight", GraphFunctionParser.Orientation.UNDIRECTED, null);
        p = new GraphFunctionParser();
        p.parseWeightAndOrientation("undirected", "weight");
        checkWeightAndOrientation(p, "weight", GraphFunctionParser.Orientation.UNDIRECTED, null);
    }

    private void checkWeightAndOrientation(GraphFunctionParser p, String weight,
                                           GraphFunctionParser.Orientation global, String local) {
        assertEquals(weight, p.getWeightColumn());
        assertEquals(global, p.getGlobalOrientation());
        assertEquals(local, p.getEdgeOrientation());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDFail() {
        parser.parseWeightAndOrientation(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWDFail1() {
        parser.parseWeightAndOrientation("weight", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWDFail2() {
        parser.parseWeightAndOrientation(null, "weight");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDoubleWeight() {
        parser.parseWeightAndOrientation("weight", "distance");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDoubleOrientation() {
        parser.parseWeightAndOrientation("undirected", "undirected");
    }

    @Test
    public void testDestinationsString() {
        assertTrue(Arrays.equals(new int[]{1, 2, 3, 4, 5},
                parser.parseDestinationsString("1, 2, 3, 4, 5")));
        assertTrue(Arrays.equals(new int[]{2343, 637, 1, 345},
                parser.parseDestinationsString("2343   ,    637,1, 345")));
        assertTrue(Arrays.equals(new int[]{1, 2},
                parser.parseDestinationsString("1, 2,,,,,,,,")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDestinationsStringFail() {
        assertTrue(Arrays.equals(new int[]{1, 2, 3},
                parser.parseDestinationsString("1, 2,,3")));
    }

    @Test
    public void testParseInputTable() throws SQLException {
        final TableLocation roads_edges = TableUtilities.parseInputTable(connection, "ROADS_EDGES");
        assertEquals("", roads_edges.getCatalog());
        assertEquals("", roads_edges.getSchema());
        assertEquals("ROADS_EDGES", roads_edges.getTable());
    }

    @Test
    public void testSuffixTableLocation() throws SQLException {
        final TableLocation roads_edges = TableUtilities.parseInputTable(connection, "ROADS_EDGES");
        final TableLocation suffixed = TableUtilities.suffixTableLocation(roads_edges, "_SUFFIX");
        assertEquals("", suffixed.getCatalog());
        assertEquals("", suffixed.getSchema());
        assertEquals("ROADS_EDGES_SUFFIX", suffixed.getTable());
    }
}

package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.routing.Edge;
import ch.epfl.javelo.routing.ElevationProfile;
import ch.epfl.javelo.routing.Route;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * GPX file route generator for JaVelo
 *
 * @author Jan Staszewicz (341201)
 * @author Elias Mir(341277)
 */
public class GpxGenerator {

    //non-instantiable
    private GpxGenerator() {}

    /**
     * Write a new gpx file with given route and elevation  profile with a given name
     *
     * @param fileName the file name
     * @param route    the route
     * @param profile  the route's profile
     * @throws IOException if there is an issue when handling the buffer
     */
    public static void writeGpx(String fileName, Route route, ElevationProfile profile) throws IOException {

        Document doc = createGpx(route, profile);

        try (Writer w = Files.newBufferedWriter(Path.of(fileName))) {
            Transformer transformer = TransformerFactory
                    .newDefaultInstance()
                    .newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(doc),
                    new StreamResult(w));
        } catch (TransformerConfigurationException e) {
            throw new Error(e); // Should never happen
        } catch (TransformerException e) {
            throw new Error(e); // Should never happen
        }
    }

    /**
     * Creates a new document for the gpx route
     *
     * @return a new document
     */
    private static Document newDocument() {
        try {
            return DocumentBuilderFactory
                    .newDefaultInstance()
                    .newDocumentBuilder()
                    .newDocument();
        } catch (ParserConfigurationException e) {
            throw new Error(e); // Should never happen
        }
    }

    /**
     * Creates the header part of the gpx file
     *
     * @param doc  the document
     * @param root the root of the document
     */
    private static void createHeader(Document doc, Element root) {

        root.setAttributeNS(
                "http://www.w3.org/2001/XMLSchema-instance",
                "xsi:schemaLocation",
                "http://www.topografix.com/GPX/1/1 " + "http://www.topografix.com/GPX/1/1/gpx.xsd");
        root.setAttribute("version", "1.1");
        root.setAttribute("creator", "JaVelo");

        Element metadata = doc.createElement("metadata");
        root.appendChild(metadata);

        Element name = doc.createElement("name");
        metadata.appendChild(name);
        name.setTextContent("Route JaVelo");
    }

    /**
     * Creates a new gpx point element and add it to a rte element
     *
     * @param doc       the document
     * @param route     the rte (route) element of the document
     * @param pointAt   the point at a certain position on the route
     * @param elevation at the point position
     */
    private static void createGpxPoint(Document doc, Element route, PointCh pointAt, double elevation) {

        Element routePoint = doc.createElement("rtept");
        route.appendChild(routePoint);

        routePoint.setAttribute("lat", String.valueOf(Math.toDegrees(pointAt.lat())));
        routePoint.setAttribute("lon", String.valueOf(Math.toDegrees(pointAt.lon())));

        Element ele = doc.createElement("ele");
        ele.setTextContent(String.valueOf(elevation));

        routePoint.appendChild(ele);
    }


    /**
     * Creates the body part of the gpx file
     *
     * @param doc     the document
     * @param root    the root of the document
     * @param route   the route element of the document
     * @param profile the route's elevation profile
     */
    private static void createBody(Document doc, Element root, Route route, ElevationProfile profile) {
        Element rte = doc.createElement("rte");
        root.appendChild(rte);

        List<Edge> edges = route.edges();
        double l = 0;

        for (Edge edge : edges) {
            if (l == 0) {
                createGpxPoint(doc, rte, route.pointAt(l), profile.elevationAt(l));
            }

            l += edge.length();
            createGpxPoint(doc, rte, route.pointAt(l), profile.elevationAt(l));
        }
    }

    /**
     * Creates a gpx document with given route and elevation profile
     *
     * @param route   the route
     * @param profile the route's elevation profile
     * @return a gpx document
     */
    public static Document createGpx(Route route, ElevationProfile profile) {

        Document doc = newDocument();

        //create root
        Element root = doc.createElementNS("http://www.topografix.com/GPX/1/1", "gpx");
        doc.appendChild(root);

        createHeader(doc, root);
        createBody(doc, root, route, profile);

        return doc;
    }
}

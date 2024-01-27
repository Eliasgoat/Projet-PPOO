package ch.epfl.javelo.gui;

import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.routing.CityBikeCF;
import ch.epfl.javelo.routing.ElevationProfile;
import ch.epfl.javelo.routing.ElevationProfileComputer;
import ch.epfl.javelo.routing.RouteComputer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;

public class GpxGeneratorTest {

    private static Graph graph;
    
    private static RouteComputer newLausanneRouteComputer() {
        if (graph == null) {
            try {
                graph = Graph.loadFrom(Path.of("lausanne"));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        var cf = new CityBikeCF(graph);
        return new RouteComputer(graph, cf);
    }

    public static void main(String[] args) throws IOException {
        var rc = newLausanneRouteComputer();
        var route = rc.bestRouteBetween(159049, 117669);

        ElevationProfile elevationProfile = ElevationProfileComputer.elevationProfile(route, 1);
        GpxGenerator.writeGpx("itinerary1_epfl_sauvabelin.gpx", route, elevationProfile);

        var route2 = rc.bestRouteBetween(158500, 118005);
        ElevationProfile elevationProfile2 = ElevationProfileComputer.elevationProfile(route2, 1);
        GpxGenerator.writeGpx("itinerary2.gpx", route2, elevationProfile2);
    }
}

/*
 * Copyright 2010-2015 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.schildbach.pte.live;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

import de.schildbach.pte.NetworkProvider.Accessibility;
import de.schildbach.pte.NetworkProvider.WalkSpeed;
import de.schildbach.pte.SvvProvider;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.NearbyLocationsResult;
import de.schildbach.pte.dto.Product;
import de.schildbach.pte.dto.QueryDeparturesResult;
import de.schildbach.pte.dto.QueryTripsResult;
import de.schildbach.pte.dto.SuggestLocationsResult;

/**
 * @author Andreas Schildbach
 */
public class SvvProviderLiveTest extends AbstractProviderLiveTest {
    public SvvProviderLiveTest() {
        super(new SvvProvider(secretProperty("svv.api_authorization")));
    }

    @Test
    public void nearbyStations() throws Exception {
        final NearbyLocationsResult result = queryNearbyStations(new Location(LocationType.STATION, "455000200"));
        print(result);
    }

    @Test
    public void nearbyStationsByCoordinate() throws Exception {
        final NearbyLocationsResult result = queryNearbyStations(Location.coord(47813093, 13045065));
        print(result);
    }

    @Test
    public void queryDepartures() throws Exception {
        final QueryDeparturesResult result = queryDepartures("455000200", false);
        print(result);
    }

    @Test
    public void queryDeparturesInvalidStation() throws Exception {
        final QueryDeparturesResult result = queryDepartures("999999", 0, false);
        assertEquals(QueryDeparturesResult.Status.INVALID_STATION, result.status);
    }

    @Test
    public void suggestLocations() throws Exception {
        final SuggestLocationsResult result = suggestLocations("Salzburg Hauptbahnhof");
        print(result);
        assertThat(result.getLocations(), hasItem(new Location(LocationType.STATION, "455000200")));
    }

    @Test
    public void suggestLocationsIncomplete() throws Exception {
        final SuggestLocationsResult result = suggestLocations("Kur");
        print(result);
    }

    @Test
    public void suggestLocationsWithUmlaut() throws Exception {
        final SuggestLocationsResult result = suggestLocations("Südstadt");
        print(result);
        assertThat(result.getLocations(), hasItem(new Location(LocationType.STATION, "900015650")));
    }

    @Test
    public void suggestLocationsAddress() throws Exception {
        final SuggestLocationsResult result = suggestLocations("Kaigasse 10");
        print(result);
    }

    @Test
    public void suggestLocationsCoverage() throws Exception {
        final SuggestLocationsResult salzburgResult = suggestLocations("Salzburg Hauptbahnhof");
        print(salzburgResult);
        assertThat(salzburgResult.getLocations(), hasItem(new Location(LocationType.STATION, "455000200")));
    }

    @Test
    public void shortTrip() throws Exception {
        final QueryTripsResult result = queryTrips(
                new Location(LocationType.STATION, "455002100", 47797110, 13053632, "Salzburg", "Justizgebäude"), null,
                new Location(LocationType.STATION, "455002200", 47794000, 13059223, "Salzburg", "Akademiestraße"),
                new Date(), true, Product.ALL, WalkSpeed.NORMAL, Accessibility.NEUTRAL);
        print(result);
        assertEquals(QueryTripsResult.Status.OK, result.status);
        assertTrue(result.trips.size() > 0);

        if (!result.context.canQueryLater())
            return;

        final QueryTripsResult laterResult = queryMoreTrips(result.context, true);
        print(laterResult);

        if (!laterResult.context.canQueryLater())
            return;

        final QueryTripsResult later2Result = queryMoreTrips(laterResult.context, true);
        print(later2Result);

        if (!later2Result.context.canQueryEarlier())
            return;

        final QueryTripsResult earlierResult = queryMoreTrips(later2Result.context, false);
        print(earlierResult);
    }
}

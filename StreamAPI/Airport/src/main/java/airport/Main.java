package airport;

import com.skillbox.airport.Aircraft;
import com.skillbox.airport.Airport;
import com.skillbox.airport.Flight;
import com.skillbox.airport.Terminal;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;


public class Main {
    public static void main(String[] args) {
        Airport testAirport = Airport.getInstance();
        findMapCountParkedAircraftByTerminalName(testAirport);
    }

    public static long findCountAircraftWithModelAirbus(Airport airport, String model) {
        return airport.getAllAircrafts()
                .stream()
                .map(Aircraft::getModel)
                .filter(sModel -> sModel.startsWith(model))
                .count();
    }

    public static Map<String, Integer> findMapCountParkedAircraftByTerminalName(Airport airport) {
        return airport.getTerminals()
                .stream()
                .collect(Collectors.toMap(
                        Terminal::getName,
                        terminal -> terminal.getParkedAircrafts().size()
                ));
    }

    public static List<Flight> findFlightsLeavingInTheNextHours(Airport airport, int hours) {
        Instant now  = Instant.now();
        Instant end  = now.plusSeconds(hours *3600L);

        return airport.getTerminals()
                .stream()
                .flatMap(t -> t.getFlights().stream())
                .filter(f->f.getType() == Flight.Type.DEPARTURE)
                .filter(f->{
                    Instant d = f.getDate();
                    return !d.isBefore(now) &!d.isAfter(end);
                })
                .sorted(Comparator.comparing(Flight::getDate))
                .collect(Collectors.toList());
    }

    public static Optional<Flight> findFirstFlightArriveToTerminal(Airport airport, String terminalName) {
        return airport.getTerminals().stream()
                .filter(t -> Objects.equals(t.getName(), terminalName))
                .flatMap(t -> t.getFlights().stream())
                .filter(f -> f.getType() == Flight.Type.ARRIVAL)
                .min(Comparator.comparing(Flight::getDate));
    }
}
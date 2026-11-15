package co.edu.unbosque.SistemaHorario.model.algoritmo;

import co.edu.unbosque.SistemaHorario.model.*;
import co.edu.unbosque.SistemaHorario.model.exception.HorarioInalcanzableException;
import co.edu.unbosque.SistemaHorario.respository.AulaRepository;
import co.edu.unbosque.SistemaHorario.respository.FranjaHorariaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Qualifier("algoritmoGeneticoHorario")
public class AlgoritmoGeneticoHorario implements IAlgoritmoHorario {

    private static final int TAMANO_POBLACION = 80;
    private static final int MAX_GENERACIONES = 500;
    private static final double PROB_CRUCE = 0.85;
    private static final double PROB_MUTACION = 0.12;
    private static final int TORNEO_K = 5;

    private static final int PESO_AULA_OCUPADA = 100;
    private static final int PESO_DOCENTE_OCUPADO = 100;
    private static final int PESO_GRUPO_OCUPADO = 100;
    private static final int PESO_FRANJA_NO_DISPONIBLE = 150;
    private static final int PESO_TIPO_AULA_INCORRECTO = 50;
    private static final int PESO_FRANJA_INACTIVA = 200;
    private static final int PESO_AULA_NO_DISPONIBLE = 200;

    private final Random rng = new Random();

    @Autowired
    private AulaRepository aulaRepository;

    @Autowired
    private FranjaHorariaRepository franjaHorariaRepository;

    private Map<String, Set<Integer>> disponibilidadDocentes = new HashMap<>();

    @Override
    public synchronized List<Seccion> generarHorario(Facultad facultad)
            throws HorarioInalcanzableException {

        List<Seccion> secciones =
                generarSeccionesBase(facultad);

        List<Aula> aulas = recolectarAulas();
        List<FranjaHoraria> franjas = recolectarFranjas();

        validarRecursos(secciones, aulas, franjas);
        disponibilidadDocentes = construirDisponibilidad(secciones);

        List<List<Seccion>> poblacion =
                inicializarPoblacion(secciones, aulas, franjas);

        List<Seccion> mejor = null;
        int mejorFitness = Integer.MAX_VALUE;

        for (int gen = 0; gen < MAX_GENERACIONES; gen++) {

            List<Integer> fitnesses = poblacion.stream()
                    .map(this::calcularFitness)
                    .collect(Collectors.toList());

            for (int i = 0; i < poblacion.size(); i++) {
                if (fitnesses.get(i) < mejorFitness) {
                    mejorFitness = fitnesses.get(i);
                    mejor = clonarIndividuo(poblacion.get(i));
                }
            }

            if (mejorFitness == 0) break;

            List<List<Seccion>> nueva = new ArrayList<>();
            nueva.add(clonarIndividuo(mejor));

            while (nueva.size() < TAMANO_POBLACION) {

                List<Seccion> padre1 =
                        seleccionTorneo(poblacion, fitnesses);

                List<Seccion> padre2 =
                        seleccionTorneo(poblacion, fitnesses);

                List<Seccion> hijo;

                if (rng.nextDouble() < PROB_CRUCE) {
                    hijo = cruzar(padre1, padre2);
                } else {
                    hijo = clonarIndividuo(padre1);
                }

                if (rng.nextDouble() < PROB_MUTACION) {
                    mutar(hijo, aulas, franjas);
                }

                nueva.add(hijo);
            }

            poblacion = nueva;
        }

        if (mejor == null || mejorFitness > 0) {
            throw new HorarioInalcanzableException(
                    "No fue posible generar un horario sin conflictos."
            );
        }

        return mejor;
    }

    private List<Seccion> generarSeccionesBase(Facultad facultad) {

        List<Seccion> secciones = new ArrayList<>();

        Set<Integer> gruposVistos = new HashSet<>();

        if (facultad == null || facultad.getCarreras() == null) {
            return secciones;
        }

        for (Carrera carrera : facultad.getCarreras()) {

            if (carrera.getPlanEstudios() == null) continue;

            for (PlanEstudios plan : carrera.getPlanEstudios()) {

                if (plan.getCursos() == null) continue;

                for (Curso curso : plan.getCursos()) {

                    if (curso.getGrupos() == null) continue;

                    for (Grupo grupo : curso.getGrupos()) {

                        if (!gruposVistos.add(grupo.getIdGrupo())) continue;

                        Seccion seccion = new Seccion();
                        seccion.setGrupo(grupo);
                        seccion.setEstadoSeccion(EstadoSeccion.ASIGNADA);

                        secciones.add(seccion);
                    }
                }
            }
        }

        return secciones;
    }

    private List<List<Seccion>> inicializarPoblacion(
            List<Seccion> secciones,
            List<Aula> aulas,
            List<FranjaHoraria> franjas) {

        List<List<Seccion>> poblacion = new ArrayList<>();

        for (int i = 0; i < TAMANO_POBLACION; i++) {
            poblacion.add(asignarAleatorio(secciones, aulas, franjas));
        }

        return poblacion;
    }

    private List<Seccion> asignarAleatorio(
            List<Seccion> secciones,
            List<Aula> aulas,
            List<FranjaHoraria> franjas) {

        List<Seccion> individuo = new ArrayList<>();

        for (Seccion original : secciones) {

            Seccion copia = copiarSeccion(original);

            copia.setAula(
                    aulas.get(rng.nextInt(aulas.size()))
            );

            copia.setFranjaHoraria(
                    franjas.get(rng.nextInt(franjas.size()))
            );

            individuo.add(copia);
        }

        return individuo;
    }

    private int calcularFitness(List<Seccion> individuo) {

        int penalizacion = 0;

        Map<String, Integer> aulas = new HashMap<>();
        Map<String, Integer> docentes = new HashMap<>();
        Map<String, Integer> grupos = new HashMap<>();

        for (Seccion s : individuo) {

            if (s.getAula() == null || s.getFranjaHoraria() == null) {
                penalizacion += PESO_AULA_NO_DISPONIBLE;
                continue;
            }

            long aulaId = s.getAula().getIdAula();
            long franjaId = s.getFranjaHoraria().getIdFranja();

            TipoAula requerida =
                    s.getGrupo().getCurso().getAulaRequerida();

            if (requerida != null &&
                    s.getAula().getTipoAula() != requerida) {
                penalizacion += PESO_TIPO_AULA_INCORRECTO;
            }

            Docente d = s.getGrupo().getDocente();

            if (d != null) {
                Set<Integer> libres = disponibilidadDocentes
                        .getOrDefault(d.getId(), Collections.emptySet());
                boolean disponible = libres.contains((int) franjaId);

                if (!disponible) {
                    penalizacion += PESO_FRANJA_NO_DISPONIBLE;
                }

                docentes.merge(d.getId() + "_" + franjaId, 1, Integer::sum);
            }

            aulas.merge(aulaId + "_" + franjaId, 1, Integer::sum);
            grupos.merge(s.getGrupo().getIdGrupo() + "_" + franjaId, 1, Integer::sum);
        }

        penalizacion += contarChoques(aulas) * PESO_AULA_OCUPADA;
        penalizacion += contarChoques(docentes) * PESO_DOCENTE_OCUPADO;
        penalizacion += contarChoques(grupos) * PESO_GRUPO_OCUPADO;

        return penalizacion;
    }

    private int contarChoques(Map<String, Integer> mapa) {
        int total = 0;
        for (Integer c : mapa.values()) {
            if (c > 1) total += c - 1;
        }
        return total;
    }

    private List<Seccion> seleccionTorneo(
            List<List<Seccion>> poblacion,
            List<Integer> fitnesses) {

        int mejor = -1;
        int mejorFitness = Integer.MAX_VALUE;

        for (int i = 0; i < TORNEO_K; i++) {

            int idx = rng.nextInt(poblacion.size());

            if (fitnesses.get(idx) < mejorFitness) {
                mejorFitness = fitnesses.get(idx);
                mejor = idx;
            }
        }

        return poblacion.get(mejor);
    }

    private List<Seccion> cruzar(List<Seccion> p1, List<Seccion> p2) {

        if (p1.size() < 2) return clonarIndividuo(p1);

        int punto = 1 + rng.nextInt(p1.size() - 1);

        List<Seccion> hijo = new ArrayList<>();

        for (int i = 0; i < p1.size(); i++) {
            hijo.add(copiarSeccion(i < punto ? p1.get(i) : p2.get(i)));
        }

        return hijo;
    }

    private void mutar(List<Seccion> individuo,
                       List<Aula> aulas,
                       List<FranjaHoraria> franjas) {

        Seccion s = individuo.get(rng.nextInt(individuo.size()));

        if (rng.nextBoolean()) {
            s.setAula(aulas.get(rng.nextInt(aulas.size())));
        } else {
            s.setFranjaHoraria(franjas.get(rng.nextInt(franjas.size())));
        }
    }

    private Seccion copiarSeccion(Seccion original) {
        Seccion copia = new Seccion();
        copia.setGrupo(original.getGrupo());
        copia.setEstadoSeccion(original.getEstadoSeccion());
        copia.setAula(original.getAula());
        copia.setFranjaHoraria(original.getFranjaHoraria());
        return copia;
    }

    private List<Seccion> clonarIndividuo(List<Seccion> original) {
        return original.stream()
                .map(this::copiarSeccion)
                .collect(Collectors.toList());
    }

    private List<Aula> recolectarAulas() {
        return new ArrayList<>(aulaRepository.findByEstadoAula(EstadoAula.DISPONIBLE));
    }

    private List<FranjaHoraria> recolectarFranjas() {
        return new ArrayList<>(franjaHorariaRepository.findByEstadoFranja(EstadoFranja.DISPONIBLE));
    }

    private Map<String, Set<Integer>> construirDisponibilidad(List<Seccion> secciones) {
        Map<String, Set<Integer>> mapa = new HashMap<>();
        for (Seccion s : secciones) {
            Docente d = s.getGrupo().getDocente();
            if (d == null || mapa.containsKey(d.getId())) continue;
            Set<Integer> ids = new HashSet<>();
            if (d.getDisponibilidad() != null) {
                for (FranjaHoraria f : d.getDisponibilidad()) ids.add(f.getIdFranja());
            }
            mapa.put(d.getId(), ids);
        }
        return mapa;
    }

    private void validarRecursos(List<Seccion> secciones,
                                 List<Aula> aulas,
                                 List<FranjaHoraria> franjas)
            throws HorarioInalcanzableException {

        if (secciones.isEmpty()) {
            throw new HorarioInalcanzableException(
                    "No hay secciones base para generar el horario.");
        }

        if (aulas.isEmpty()) {
            throw new HorarioInalcanzableException(
                    "No hay aulas disponibles.");
        }

        if (franjas.isEmpty()) {
            throw new HorarioInalcanzableException(
                    "No hay franjas disponibles.");
        }
    }
}

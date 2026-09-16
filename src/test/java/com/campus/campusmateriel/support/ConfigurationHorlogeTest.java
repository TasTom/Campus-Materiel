package com.campus.campusmateriel.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Remplace l'horloge de l'application par une horloge fixe dans les tests.
 *
 * <p>L'enonce impose la date de recette du <strong>10 mars 2030</strong>. Sans horloge
 * fixe, les scenarios portant sur les dates (T04 date passee, T06 annulation le jour
 * reserve, T11 reservation deja annulee) dependraient du jour d'execution et
 * deviendraient faux avec le temps (FR-024, principe V de la constitution).</p>
 *
 * <p>Le bean est marque {@link Primary} pour prendre la place de celui de
 * {@code ClockConfiguration} sans modifier le code de production.</p>
 */
@TestConfiguration
public class ConfigurationHorlogeTest {

    /** Date courante des tests : le 10 mars 2030, imposee par l'enonce. */
    public static final LocalDate DATE_RECETTE = LocalDate.of(2030, 3, 10);

    /** La veille de la date de recette : utilisée pour les dates passées. */
    public static final LocalDate DATE_PASSEE = LocalDate.of(2030, 3, 9);

    /** Lendemain de la date de recette : utilisée pour les dates futures. */
    public static final LocalDate DATE_FUTURE = LocalDate.of(2030, 3, 12);

    /** Autre date future, pour verifier qu'une reservation ne bloque qu'une seule date. */
    public static final LocalDate DATE_FUTURE_AUTRE = LocalDate.of(2030, 3, 13);

    @Bean
    @Primary
    public Clock clockDeTest() {
        return Clock.fixed(
                DATE_RECETTE.atTime(12, 0).toInstant(ZoneOffset.UTC),
                ZoneOffset.UTC);
    }
}

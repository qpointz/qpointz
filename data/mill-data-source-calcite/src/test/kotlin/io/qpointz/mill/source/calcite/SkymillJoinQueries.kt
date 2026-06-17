package io.qpointz.mill.source.calcite

/**
 * Skymill SQL used by join-plan and optimization tests in this module's unit test suite.
 *
 * Dataset wiring lives in [io.qpointz.mill.test.data.skymill.SkymillTestFixtures].
 */
object SkymillJoinQueries {

    val JOIN_WITHOUT_WHERE: String =
        """
        SELECT COUNT(*) AS cnt
        FROM `skymill`.`bookings` AS b
        INNER JOIN `skymill`.`passenger` AS p ON b.`passenger_id` = p.`id`
        INNER JOIN `skymill`.`flight_instances` AS fi ON b.`flight_instance_id` = fi.`id`
        INNER JOIN `skymill`.`segments` AS s ON fi.`segment_id` = s.`id`
        INNER JOIN `skymill`.`cities` AS c1 ON s.`origin` = c1.`id`
        INNER JOIN `skymill`.`cities` AS c2 ON s.`destination` = c2.`id`
        INNER JOIN `skymill`.`cities` AS c3 ON p.`domicile_city_id` = c3.`id`
        """.trimIndent()

    val FULL_JOIN_WITH_CITIES_FILTER: String =
        """
        SELECT COUNT(*) AS cnt
        FROM `skymill`.`bookings` AS b
        INNER JOIN `skymill`.`passenger` AS p ON b.`passenger_id` = p.`id`
        INNER JOIN `skymill`.`flight_instances` AS fi ON b.`flight_instance_id` = fi.`id`
        INNER JOIN `skymill`.`segments` AS s ON fi.`segment_id` = s.`id`
        INNER JOIN `skymill`.`cities` AS c1 ON s.`origin` = c1.`id`
        INNER JOIN `skymill`.`cities` AS c2 ON s.`destination` = c2.`id`
        INNER JOIN `skymill`.`cities` AS c3 ON p.`domicile_city_id` = c3.`id`
        WHERE c2.`id` = c3.`id`
        """.trimIndent()
}

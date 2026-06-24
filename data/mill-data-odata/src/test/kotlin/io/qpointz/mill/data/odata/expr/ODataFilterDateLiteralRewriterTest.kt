package io.qpointz.mill.data.odata.expr

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class ODataFilterDateLiteralRewriterTest {

    @Test
    fun shouldRewriteBareIsoDateTimeToCalendarDate() {
        val rewritten = ODataFilterDateLiteralRewriter.rewriteFilterExpression(
            "departure_date ge 2026-03-30T22:00:00.000Z",
        )

        assertThat(rewritten).isEqualTo("departure_date ge 2026-03-30")
    }

    @Test
    fun shouldRewriteQuotedDateTimeOffsetLiteral() {
        val rewritten = ODataFilterDateLiteralRewriter.rewriteFilterExpression(
            "departure_date ge datetimeoffset'2026-03-30T22:00:00.000Z'",
        )

        assertThat(rewritten).isEqualTo("departure_date ge 2026-03-30")
    }

    @Test
    fun shouldRewriteDateTimeOffsetInCompoundFilter() {
        val rewritten = ODataFilterDateLiteralRewriter.rewriteFilterExpression(
            "departure_date ge 2026-03-30T22:00:00.000Z and departure_date lt 2026-04-30T22:00:00.000Z",
        )

        assertThat(rewritten).isEqualTo(
            "departure_date ge 2026-03-30 and departure_date lt 2026-04-30",
        )
    }

    @Test
    fun shouldRewriteFilterParameterInRequestUri() {
        val rewritten = ODataFilterDateLiteralRewriter.rewriteRequestUri(
            "http://localhost/services/odata/skymill.svc/cargo_flights?" +
                "\$filter=departure_date%20ge%202026-03-30T22%3A00%3A00.000Z",
        )

        assertThat(rewritten).contains("cargo_flights?")
        val filterValue = rewritten.substringAfter('$').substringAfter('=')
            .let { URLDecoder.decode(it, StandardCharsets.UTF_8) }
        assertThat(filterValue).isEqualTo("departure_date ge 2026-03-30")
    }

    @Test
    fun shouldRewriteFilterParameterWhenNameIsUrlEncoded() {
        val rewritten = ODataFilterDateLiteralRewriter.rewriteRequestUri(
            "http://localhost/services/odata/skymill.svc/cargo_flights?" +
                "%24filter=departure_date+ge+2026-03-30T22%3A00%3A00.000Z",
        )

        assertThat(rewritten).isEqualTo(
            "http://localhost/services/odata/skymill.svc/cargo_flights?" +
                "%24filter=departure_date+ge+2026-03-30",
        )
    }

    @Test
    fun shouldRewriteSingleQuotedIsoDateTime() {
        val rewritten = ODataFilterDateLiteralRewriter.rewriteFilterExpression(
            "departure_date ge '2026-03-30T22:00:00.000Z'",
        )

        assertThat(rewritten).isEqualTo("departure_date ge 2026-03-30")
    }

    @Test
    fun shouldRewriteDatetimeQuotedLiteral() {
        val rewritten = ODataFilterDateLiteralRewriter.rewriteFilterExpression(
            "departure_date ge datetime'2026-03-30T22:00:00.000Z'",
        )

        assertThat(rewritten).isEqualTo("departure_date ge 2026-03-30")
    }

    @Test
    fun shouldLeaveNonDateLiteralsUntouched() {
        val filter = "seat_number eq 'AA5' or id eq 138148"
        assertThat(ODataFilterDateLiteralRewriter.rewriteFilterExpression(filter)).isEqualTo(filter)
    }
}

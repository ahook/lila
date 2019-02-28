package views.html.board

import play.api.libs.json.JsObject

import chess.variant.Crazyhouse

import lila.api.Context
import lila.app.templating.Environment._
import lila.app.ui.ScalatagsTemplate._
import lila.common.String.html.safeJsonValue
import lila.rating.PerfType.iconByVariant

import controllers.routes

object userAnalysis {

  def apply(data: JsObject, pov: lila.game.Pov)(implicit ctx: Context) = views.html.base.layout(
    title = trans.analysis.txt(),
    moreCss = frag(
      responsiveCssTag {
        if (pov.game.variant == Crazyhouse) "analyse.zh"
        else "analyse"
      },
      (!pov.game.synthetic && pov.game.playable && ctx.me.flatMap(pov.game.player).isDefined) option cssTag("forecast.css")
    ),
    moreJs = frag(
      analyseTag,
      analyseNvuiTag,
      embedJs(s"""lichess=lichess||{};lichess.user_analysis={data:${safeJsonValue(data)},i18n:${
        userAnalysisI18n(
          withForecast = !pov.game.synthetic && pov.game.playable && ctx.me.flatMap(pov.game.player).isDefined
        )
      },explorer:{endpoint:"$explorerEndpoint",tablebaseEndpoint:"$tablebaseEndpoint"}};""")
    ),
    side = pov.game.synthetic option views.html.base.bits.mselect(
      "analyse-variant",
      span(dataIcon := iconByVariant(pov.game.variant))(pov.game.variant.name),
      chess.variant.Variant.all.filter(chess.variant.FromPosition !=).map { v =>
        a(dataIcon := iconByVariant(v), href := routes.UserAnalysis.parse(v.key))(v.name)
      }
    ),
    responsive = true,
    chessground = false,
    openGraph = lila.app.ui.OpenGraph(
      title = "Chess analysis board",
      url = s"$netBaseUrl${routes.UserAnalysis.index.url}",
      description = "Analyse chess positions and variations on an interactive chess board"
    ).some,
    zoomable = true
  ) {
      main(cls := "analyse")(
        st.aside(cls := "analyse__side")(spinner),
        div(cls := "analyse__board main-board")(chessgroundSvg),
        div(cls := "analyse__tools"),
        div(cls := "analyse__controls")
      )
    }
}
package ru.neoflex.transaction

import spray.json.{DefaultJsonProtocol, DeserializationException, JsNumber, JsObject, JsString, JsValue, RootJsonFormat}

object ParticipantJsonProtocol extends DefaultJsonProtocol {

  implicit object ParticipantInitializerData extends RootJsonFormat[ParticipantData] {

    override def read(json: JsValue): ParticipantData = {
      json.asJsObject.getFields("id", "balance") match {
        case Seq(JsString(id), JsNumber(balance)) =>
          ParticipantData(id, balance.intValue())
        case _ => throw new DeserializationException("Wrong format data for ParticipantData. Expect id, balance")
      }
    }

    override def write(content: ParticipantData): JsValue = JsObject(
      "id" -> JsString(content.id),
      "balance" -> JsNumber(content.balance)
    )
  }
}

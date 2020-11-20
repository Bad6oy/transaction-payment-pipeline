package ru.neoflex.payment.functions

import org.apache.flink.api.common.state.MapState
import org.apache.flink.streaming.api.functions.co.RichCoFlatMapFunction
import org.apache.flink.util.Collector
import ru.neoflex.payment.FormattedPayment
import ru.neoflex.transaction.{LoggingMessage, ParticipantData}
import ru.neoflex.utility.Messages.{notEnoughMoneyMessage, successfulOperationMessage, userNotFoundMessage}

class PaymentParticipantProcessing extends RichCoFlatMapFunction[FormattedPayment, ParticipantData, LoggingMessage] {

  @transient var storedParticipantBalance: MapState[String, Int] = _

  override def flatMap1(value: FormattedPayment, out: Collector[LoggingMessage]): Unit = {
    val sender = value.from
    if (storedParticipantBalance.contains(sender)) {
      reduceSenderBalance(value, out)
    } else {
      out.collect(userNotFoundMessage(sender))
    }
  }

  override def flatMap2(value: ParticipantData, out: Collector[LoggingMessage]): Unit = {
      storedParticipantBalance.put(value.id, value.balance)
  }

  private def reduceSenderBalance(paymentInfo: FormattedPayment, out: Collector[LoggingMessage]): Unit = {
    val before = storedParticipantBalance.get(paymentInfo.from)
    if (before <= paymentInfo.amount) {
      out.collect(notEnoughMoneyMessage(before))
      return
    }

    val receiver = paymentInfo.to
    if (!storedParticipantBalance.contains(receiver)) {
      out.collect(userNotFoundMessage(receiver))
      return
    }

    val after = before - paymentInfo.amount
    storedParticipantBalance.put(paymentInfo.from, after)
    out.collect(successfulOperationMessage(before, after))
    increaseReceiverBalance(receiver, paymentInfo.amount, out)
  }

  private def increaseReceiverBalance(receiver: String, amount: Int, out: Collector[LoggingMessage]): Unit = {
    val before = storedParticipantBalance.get(receiver)
    val after = before + amount
    storedParticipantBalance.put(receiver, before)
    out.collect(successfulOperationMessage(before, after))
  }
}

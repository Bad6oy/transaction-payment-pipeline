package ru.neoflex.utility

import ru.neoflex.payment.FormattedPayment
import ru.neoflex.transaction.LoggingMessage

object Messages {

  private val NOT_ENOUGH: String = "Balance too low. Your balance is: "
  private val EMPTY_INFO: String = "is empty"
  private val BALANCE_BEFORE: String = "Your balance was: "
  private val SUCCESSFUL_TRANSACTION: String = "Transaction was successful. Your balance is: "
  private val NOT_FOUND: String = "User not found."
  private val NAME: String = "You try to find name: "
  private val INVALID_PAYMENT_MESSAGE: String = "Message must have format <NAME1> -> <NAME2>: <VALUE>"

  def notEnoughMoneyMessage(balance: Int): LoggingMessage = {
    new LoggingMessage(message = NOT_ENOUGH + balance, content = EMPTY_INFO,
      logLevel = WARNING.level)
  }

  def successfulOperationMessage(before: Int, after: Int): LoggingMessage = {
    new LoggingMessage(message = SUCCESSFUL_TRANSACTION + after, content = BALANCE_BEFORE + before,
      logLevel = INFO.level)
  }

  def userNotFoundMessage(name: String): LoggingMessage = {
    new LoggingMessage(message = NOT_FOUND, content = NAME + name,
      logLevel = WARNING.level)
  }

  def invalidPaymentDataMessage(string: String): LoggingMessage = {
    new LoggingMessage(message = INVALID_PAYMENT_MESSAGE, content = string,
      logLevel = WARNING.level)
  }

  def formattedPaymentMessage(from: String, to: String, amount: String): FormattedPayment = {
    val intAmount = amount.toInt
    new FormattedPayment(from = from, to = to, amount = intAmount)
  }
}

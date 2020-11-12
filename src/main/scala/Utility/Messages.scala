package Utility

import logging.{INFO, WARNING}
import payment.FormattedPayment
import transaction.LoggingMessage

object Messages {

  private val NOT_ENOUGH: String = "Balance too low. Your balance is: "
  private val EMPTY_INFO: String = "is empty"
  private val BALANCE_BEFORE: String = "Your balance was: "
  private val SUCCESSFUL_TRANSACTION: String = "Transaction was successful. Your balance is: "
  private val NOT_FOUND: String = "User not found."
  private val NAME: String = "You try to find name: "
  private val INVALID_PAYMENT_MESSAGE: String = "Message must have format <NAME1> -> <NAME2>: <VALUE>"

  def notEnoughMoneyMessage(balance: Int): LoggingMessage = {
    new LoggingMessage(NOT_ENOUGH + balance, EMPTY_INFO, WARNING.level)
  }

  def successfulOperationMessage(before: Int, after: Int): LoggingMessage = {
    new LoggingMessage(SUCCESSFUL_TRANSACTION + after, BALANCE_BEFORE + before, INFO.level)
  }

  def userNotFoundMessage(name: String): LoggingMessage = {
    new LoggingMessage(NOT_FOUND, NAME + name, WARNING.level)
  }

  def invalidPaymentDataMessage(string: String): LoggingMessage = {
    new LoggingMessage(WARNING.level, INVALID_PAYMENT_MESSAGE, string)
  }

  def formattedPaymentMessage(from: String, to: String, amount: String): FormattedPayment = {
    val intAmount = amount.toInt
    new FormattedPayment(from, to, intAmount)
  }
}

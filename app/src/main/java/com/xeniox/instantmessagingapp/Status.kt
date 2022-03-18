package com.xeniox.instantmessagingapp

class Status(val lastSeen: Long) { // Create a class called User
    //created blank constructor as kotlin now requires this when making a class
    constructor() : this(-1) // Create a constructor that takes in a long and sets it to lastSeen
}
package me.nunogneto.configuration;

import java.util.regex.Pattern;

public record URLShortenerConfiguration(int maxSize, Pattern allowedCharacters) { }

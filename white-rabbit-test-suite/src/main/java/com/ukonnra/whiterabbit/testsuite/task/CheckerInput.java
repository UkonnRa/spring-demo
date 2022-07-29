package com.ukonnra.whiterabbit.testsuite.task;

public record CheckerInput<I extends TaskInput, R>(I input, R result) {}

package com.client.handlers;

import com.client.network.AsyncClient;
import com.common.Response;
import java.util.Scanner;

public interface CommandHandler {
    Response handle(AsyncClient client, String[] args, Scanner scanner) throws Exception;
}
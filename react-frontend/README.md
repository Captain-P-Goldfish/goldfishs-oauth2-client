# How to setup

1. Execute `mvn process-resources -DskipTests -Pnpm-install` to initiate the react project.
2. Start the application spring-boot module. It will run on port 8080.
3. Make sure that packages.json has the definition `"proxy": "localhost:8080"`
4. Execute `npm start`
                        
# How to build

1. Execute `mvn process-resources -DskipTests -Pnpm-build`
2. Copy the generated output into the
   directory `application/src/main/resources/de/captaingoldfish/restclient/application`
3. Now the application should be available under `http://localhost:8008`

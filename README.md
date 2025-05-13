Projeto pra faculdade,  com o tempo gostaria de adicionar mais funcionalidades como:

 Registro de IP com nome. (Feito)
 
 Consulta com sockets. (Feito)

Persistência em arquivo. (Feito)

Replicação entre servidores (servidor distribuído).

Cache no cliente.

Autenticação básica.

Interface de testes ou painel web (opcional).

ExecutorService para controlar acessos simultaneos. (Feito)

Timeouts e logs.

Poder enviar uma consulta que atinge vários servidores e retorna o primeiro que responder.

IMPORTANTE:
Cada classe tem sua responsabilidade bem definida seguindo princípios SOLID:
- **MainClient/MainServer**: Pontos de entrada
- **nomeServidor**: Lógica principal do servidor
- *Resolver**: Cliente e comunicação
- **FileManager**: Persistência
- **ConfigManager**: Configurações


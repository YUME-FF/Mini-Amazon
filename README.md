# Integration of E-Commerce and Logistics Systems: The Mini-Amazon

Our project, Mini-Amazon, is a simulated e-commerce and delivery platform designed to mirror the complex interactions of online shopping and logistics. Built with Django for the web interface and Java for backend operations, it showcases the integration of these components with real-time updates facilitated by Protocol Buffers and PostgreSQL. This educational model provides insight into the seamless coordination required for modern digital marketplaces and lays the groundwork for further exploration into efficient system design.

![](/img/miniproj.png)
Figure.1. Bare Minimum Functionality Diagram[1]


# How to run
Make sure you have install the packages:

```
Django>3.0
psycopg2
django-mathfilters>=0.4.0
```

After ```git pull```, Go to ./Mini-Amazon-Django/web-app/

Change database based on your own

Then back to last directory:
```
python3 manage.py makemigrations
python3 manage.py migrate
```

Start your server by
```
python3 manage.py runserver 0:8000
```
*Using 0.0.0.0 means the server runs on "all" interfaces on the machine, including the one which has a public IP.

Then you can go to ./mini_amazon/ to run the java backend code.

# Objectives
+ Create a user-friendly e-commerce web interface with Django.
+ Simulate order processing and delivery using Java and a mock logistics platform.
+ Maintain data consistency across the system with PostgreSQL.
+ Enable efficient system communication with Protocol Buffers.
+ Design for scalability and real-time user updates.

# Methods
![](/img/world_amazon.svg) 
Figure.2. Amazon and World Simulator Interaction 

![](/img/ups_amazon.svg)
Figure.3. UPS and Amazon Communication Protocol


+ Web Application: The Django framework was chosen for its maturity and comprehensive features to create a responsive web interface.
+ Backend Logic: Java served as the backbone for backend services, providing robust and efficient order processing capabilities.
+ Data Storage: PostgreSQL was utilized for data persistence, with Hibernate enabling seamless object-relational mapping.
+ Communication Protocol: Protocol Buffers were implemented for streamlined and structured data exchange between the web application and logistics services.

# System Design
![](/img/mermaid-diagram-2024-04-02-183407.png)
Figure.4. System Interaction Flowchart: From Order Placement to Delivery

# Web Application Interface
![](/img/login.png)
Figure.5. Home page

![](/img/login2.png)
Figure.6. Login page

![](/img/profile.png)
Figure.7. Profile page

![](/img/mainpage.png)
Figure.8. Main page

![](/img/order.png)
Figure.9. Order history  page

# System Evaluation
![](/img/analysis.png)
Figure.10. System Performance Metrics: Transaction Processing, Scalability, Consistency, and Reliability

+ Transaction Processing Efficiency: The average transaction time maintains at about 1.5 seconds, with minimum times around 1.2 seconds and peaks just under 2 seconds. These figures reflect a capable processing speed, though peak times indicate occasional delays that may be optimized.
+ Scalability Dynamics: Latency incrementally rises from 1 to 2 seconds as the user count increases to 100. This steady increase, rather than a sharp spike, suggests the system scales adequately, but highlights a potential for performance improvement at higher user loads.
+ Data Consistency Trends: Success rates start near 100% but show a decline, bottoming around 97%. This gradual decrease calls for a closer look at the system’s consistency under varied transactional loads.
+ Transaction Error Rate Assessment: Error rates grow from near-zero to stabilize around 3%. This pattern indicates effective error management but also points to the necessity for early detection and prevention of errors as part of system optimization efforts.

# Conclusion
The Mini-Amazon project effectively simulates an e-commerce and logistics environment, achieving an average transaction time of 1.5 seconds and showcasing scalability. Despite the observed decline in consistency rates and a stabilization of error rates at 3%, the system shows significant potential. Future enhancements, including an upgrade from proto2 to proto3, are expected to optimize performance and prepare the system for expanded operations, underlining the continuous evolution of our technological solutions.

# Reference
[1] ECE568 Course Project: Mini-Amazon / Mini-UPS. Tyler Bletsch
https://people.duke.edu/~tkb13/.
[2] Django Software Foundation. (2021). Django Documentation. Retrieved from https://docs.djangoproject.com/
[3] Google. (2021). gRPC Documentation. Retrieved from https://grpc.io/docs/
[4] PostgreSQL Global Development Group. (2021). PostgreSQL 13.3 Documentation. Retrieved from https://www.postgresql.org/docs/13/index.html
[5] Protocol Buffers. (2021). Protocol Buffers Developer Guide. Retrieved from https://developers.google.com/protocol-buffers/docs/overview
# Integration of E-Commerce and Logistics Systems: The Mini-Amazon

Our project, Mini-Amazon, is a simulated e-commerce and delivery platform designed to mirror the complex interactions of online shopping and logistics. Built with Django for the web interface and Java for backend operations, it showcases the integration of these components with real-time updates facilitated by Protocol Buffers and PostgreSQL. This educational model provides insight into the seamless coordination required for modern digital marketplaces and lays the groundwork for further exploration into efficient system design.

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

# Methods

# System Design

# System Evaluation

# Conclusion

# Reference
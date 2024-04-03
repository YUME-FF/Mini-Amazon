from django.db import models
from django.contrib.auth.models import User
from django.utils import timezone
# Create your models here.


class Category(models.Model):
    category = models.CharField(max_length=100)

    def __str__(self):
        return self.category


class Product(models.Model):
    description = models.CharField(max_length=200, null=False, blank=False)
    category = models.ForeignKey(
        Category, related_name='products', on_delete=models.SET_NULL, null=True)
    price = models.FloatField(default=9.99, null=False, blank=False)

    def __str__(self):
        return f'<{self.description}, {self.price}>'


class Package(models.Model):
    customer = models.ForeignKey(
        User, related_name='packages', on_delete=models.CASCADE)
    address_x = models.IntegerField(default=1)
    address_y = models.IntegerField(default=1)
    status = models.CharField(max_length=50, default="processing")
    track_num = models.IntegerField(default=-1)

    def __str__(self):
        return f"{self.customer}'s package: {self.status}"


class Order2(models.Model):
    customer = models.ForeignKey(
        User, related_name='orders', on_delete=models.CASCADE)
    product = models.ForeignKey(
        Product, related_name='orders', on_delete=models.CASCADE)
    quantity = models.IntegerField(default=1)
    package = models.ForeignKey(
        Package, related_name='orders', on_delete=models.CASCADE)
    order_time = models.DateTimeField(default=timezone.now)
    status = models.CharField(max_length=50, default="processing")

    def __str__(self):
        return f"{self.customer}'s order of {self.product} at {self.package}, {self.status}"


class Order(models.Model):
    '''
    this is the class for the communication 
    between the Django and Java Backend
    '''
    shipID = models.BigAutoField(primary_key=True)
    whID = models.IntegerField(null=True)
    truckID = models.IntegerField(null=True)
    locationX = models.IntegerField(null=True)
    locationY = models.IntegerField(null=True)
    productID = models.IntegerField(null=True)
    productDescription = models.CharField(max_length=255, null=True)
    productCount = models.IntegerField(null=True)
    status = models.CharField(max_length=255)
    userID = models.IntegerField(null=True)

    class Meta:
        db_table = 'orders'

    def __str__(self):
        return f'{self.productCount} {self.productDescription} in ship id:  {self.shipID} with status: {self.status}'

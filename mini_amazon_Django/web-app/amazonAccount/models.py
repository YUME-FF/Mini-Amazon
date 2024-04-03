from django.db import models
from django.contrib.auth.models import User

# Create your models here.


class UserAccount(models.Model):
    user = models.OneToOneField(
        User, primary_key=True, on_delete=models.CASCADE)
    phone = models.BigIntegerField(default=0)
    creditCard = models.BigIntegerField(default=0)

    def __str__(self):
        return f'{self.user.username} Account Info'


class Address(models.Model):
    owner = models.ForeignKey(
        UserAccount, related_name='addresses', on_delete=models.CASCADE)
    addressName = models.CharField(max_length=200, default="")
    address_x = models.IntegerField(default=-1, blank=True)
    address_y = models.IntegerField(default=-1, blank=True)

    def __str__(self):
        return f'{self.addressName}: <{self.address_x},{self.address_y}>'

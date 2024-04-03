from django.apps import AppConfig
from django.db.models.signals import post_migrate
from django.core.exceptions import ObjectDoesNotExist


def createUser(myName, myEmail, myPassword):
    from django.contrib.auth.models import User
    try:
        User.objects.get(username=myName)
    except ObjectDoesNotExist:
        user = User.objects.create(
            username=myName,
            email=myEmail,
            is_superuser=False
        )
        user.set_password(myPassword)
        user.save()


def createCategory(myCategoryName):
    from .models import Category
    if Category.objects.all().count() == 0:
        for name in myCategoryName:
            Category.objects.create(category=name)


def createProduct(descriptions, myProductPrice, myProductCategory):
    from .models import Product, Category
    assert len(descriptions) == len(myProductPrice) and len(
        descriptions) == len(myProductCategory)
    length = len(descriptions)
    if Product.objects.all().count() == 0:
        for i in range(length):
            cat = Category.objects.get(category=myProductCategory[i])
            Product.objects.create(description=descriptions[i],
                                   category=cat,
                                   price=myProductPrice[i]
                                   )


def initStock(sender, **kwargs):
    createUser("admin", "zf70@duke.edu", "12345678")
    createCategory(["Electronics", "Books", "Clothes"])
    createProduct(["TV", "Book", "Shirt"], [100.12, 10.00, 20.50],
                  ["Electronics", "Books", "Clothes"])


class AmazonwebConfig(AppConfig):
    default_auto_field = 'django.db.models.BigAutoField'
    name = 'amazonWeb'

    def ready(self):
        post_migrate.connect(initStock, sender=self)

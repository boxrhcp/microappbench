from django.http import HttpResponse
from django.template import loader
from django.views.decorators.clickjacking import xframe_options_exempt
import os


@xframe_options_exempt
def index(request):
    template = loader.get_template('results/dashboard.html')
    base = os.path.dirname(os.path.abspath(__file__))
    file_list = os.listdir(base + '/templates/results/files')
    pattern_list = []
    for file in file_list:
        names = file.split("-")
        if names[0] + "-" + names[1] not in pattern_list:
            pattern_list.append(names[0] + "-" + names[1])

    context = {
        'file_list': file_list,
        'pattern_list': pattern_list
    }
    return HttpResponse(template.render(context, request))
